package com.ardao.nakitera_case_study.service.impl;

import com.ardao.nakitera_case_study.entity.*;
import com.ardao.nakitera_case_study.entity.box.MatchedOrderOutbox;
import com.ardao.nakitera_case_study.entity.box.OrderOutbox;
import com.ardao.nakitera_case_study.enums.Status;
import com.ardao.nakitera_case_study.exception.custom.CustomerNotFoundException;
import com.ardao.nakitera_case_study.exception.custom.OrderCannotBeCanceledException;
import com.ardao.nakitera_case_study.exception.custom.OrderNotFoundException;
import com.ardao.nakitera_case_study.util.mapper.order.OrderMapper;
import com.ardao.nakitera_case_study.model.OrderBook;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.repository.box.MatchedOrderOutboxRepository;
import com.ardao.nakitera_case_study.repository.OrderRepository;
import com.ardao.nakitera_case_study.repository.box.OrderOutboxRepository;
import com.ardao.nakitera_case_study.response.OrderListResponse;
import com.ardao.nakitera_case_study.service.OrderService;
import com.ardao.nakitera_case_study.util.resolver.CustomerIdResolver;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final MatchedOrderOutboxRepository matchedOrderOutboxRepository;
    private final CustomerIdResolver customerIdResolver;



    public OrderServiceImpl(CustomerRepository customerRepository,
                            OrderRepository orderRepository,
                            OrderOutboxRepository orderOutboxRepository,
                            MatchedOrderOutboxRepository matchedOrderOutboxRepository, CustomerIdResolver customerIdResolver) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderOutboxRepository = orderOutboxRepository;
        this.matchedOrderOutboxRepository = matchedOrderOutboxRepository;
        this.customerIdResolver = customerIdResolver;
    }


    @Override
    @Transactional
    public void createOrder(Order order, Long customerId) {
        long id = this.customerIdResolver.resolveCustomerId(customerId);
        Customer customer = this.customerRepository.findById(id)
                              .orElseThrow(() -> new CustomerNotFoundException("customer.not.found.exception", id));

        log.info("Creating order. customerId={}, assetName={}, side={}, size={}, price={}",
                id, order.getAssetName(), order.getOrderSide(), order.getSize(), order.getPrice());

        order.setCustomer(customer);
        order.setCreateDate(Instant.now());
        order = this.orderRepository.saveAndFlush(order);

        OrderOutbox orderOutboxEvent = createOrderCreatedOutboxEvent(customer,order);

      this.orderOutboxRepository.save(orderOutboxEvent);

        log.info("Order created. orderId={}, customerId={}, outboxEventType={}",
                order.getId(), customer.getId(), "ORDER_CREATED");
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("Cancel requested. orderId={}", orderId);
        Order order = this.orderRepository.findById(orderId)
                                .orElseThrow(() -> new OrderNotFoundException("order.not.found.exception", orderId));

        canCancelOrder(order);

        if(order.getOrderStatus() == Status.PENDING){

            OrderOutbox orderOutboxEvent = new OrderOutbox();
            orderOutboxEvent.setOrderId(order.getId());
            orderOutboxEvent.setCustomerId(order.getCustomer().getId());
            orderOutboxEvent.setAssetName(order.getAssetName());
            orderOutboxEvent.setOrderSide(order.getOrderSide());
            orderOutboxEvent.setPrice(order.getPrice());
            orderOutboxEvent.setSize(order.getSize());
            orderOutboxEvent.setEventType("ORDER_CANCELED");

            this.orderOutboxRepository.save(orderOutboxEvent);
            order.setOrderStatus(Status.CANCELED);
            log.info("Order canceled. orderId={}, customerId={}",
                    order.getId(), order.getCustomer().getId());

        }else{
            log.warn("Cancel rejected because order is not pending. orderId={}, status={}",
                    order.getId(), order.getOrderStatus());
            throw new OrderCannotBeCanceledException("order.cannot.be.canceled.exception", orderId);
        }

    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("order.not.found.exception", orderId));

        if(order.getOrderStatus() == Status.PENDING){
            log.info("Order rejected. orderId={}, currentStatus={}", orderId, order.getOrderStatus());
            order.setOrderStatus(Status.REJECTED);
        }

    }

    @Override
    @Transactional
    public void matchOrders() {
       List<Order> pendingOrders = this.orderRepository.findAllByOrderStatus(Status.PENDING);
          Map<String, OrderBook> orderBooks = new HashMap<>();

        log.info("Matching started.");


          pendingOrders.stream().forEach(order -> {
              String key = order.getAssetName() + "-" + order.getSize();
               OrderBook book =  orderBooks.computeIfAbsent(key, k -> new OrderBook());
               book.addOrder(order);
          });

          orderBooks.values().forEach(book -> {
              while (!book.getBuyQueue().isEmpty() && !book.getSellQueue().isEmpty()){

                  Order buyOrder = book.getBuyQueue().peek();
                  Order sellOrder = book.getSellQueue().peek();

                  if((buyOrder.getPrice() - sellOrder.getPrice()) < 0){
                      break;
                  }

                  book.getBuyQueue().poll();
                  book.getSellQueue().poll();

                  buyOrder.setOrderStatus(Status.MATCHED);
                  sellOrder.setOrderStatus(Status.MATCHED);

                  MatchedOrderOutbox matchedOrderOutbox = new MatchedOrderOutbox();
                  matchedOrderOutbox.setBuyOrderId(buyOrder.getId());
                  matchedOrderOutbox.setSellOrderId(sellOrder.getId());
                  matchedOrderOutbox.setBuyCustomerId(buyOrder.getCustomer().getId());
                  matchedOrderOutbox.setSellCustomerId(sellOrder.getCustomer().getId());
                  matchedOrderOutbox.setAssetName(buyOrder.getAssetName());
                  matchedOrderOutbox.setMatchedSize(buyOrder.getSize());
                  matchedOrderOutbox.setBuyPrice(buyOrder.getPrice());
                  matchedOrderOutbox.setSellPrice(sellOrder.getPrice());

                  this.matchedOrderOutboxRepository.save(matchedOrderOutbox);
              }
              log.info("Matching finished.");
          });
    }

        @Override
        public Page<OrderListResponse> getCustomerOrders(int page,
                                                         int size,
                                                         Long customerId,
                                                         LocalDate startDate,
                                                         LocalDate endDate) {
            long id = this.customerIdResolver.resolveCustomerId(customerId);
            log.info("Getting order list of customer id: {}.",id);
            PageRequest pageRequest = PageRequest.of(page,size);

            LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.of(1923,10,29);
            LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();

            Instant startDateInstant = resolvedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endDateInstant = resolvedEndDate.plusDays(1)
                                                          .atStartOfDay(ZoneId.systemDefault())
                                                               .toInstant().minusMillis(1);


            Page<OrderListResponse> pageList = this.orderRepository.getOrdersByCustomer_IdAndCreateDateBetween(
                    id,
                    startDateInstant,
                    endDateInstant,
                    pageRequest
            ).map(OrderMapper::toOrderListResponse);

            return pageList;
        }



    private OrderOutbox createOrderCreatedOutboxEvent(Customer customer, Order order){
        OrderOutbox orderOutboxEvent = new OrderOutbox();
        orderOutboxEvent.setOrderId(order.getId());
        orderOutboxEvent.setCustomerId(customer.getId());
        orderOutboxEvent.setEventType("ORDER_CREATED");
        orderOutboxEvent.setAssetName(order.getAssetName());
        orderOutboxEvent.setOrderSide(order.getOrderSide());
        orderOutboxEvent.setPrice(order.getPrice());
        orderOutboxEvent.setSize(order.getSize());
        return orderOutboxEvent;
    }

    private void canCancelOrder(Order order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isUser = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));

        if (isUser) {
            User currentUser = (User) auth.getPrincipal();

            if ((currentUser.getCustomer().getId() != order.getCustomer().getId())) {
                log.warn("Cancel denied. orderId={}, requesterCustomerId={}, ownerCustomerId={}",
                        order.getId(), currentUser.getCustomer().getId(), order.getCustomer().getId());

                throw new AccessDeniedException("access.denied.exception");

            }
        }
    }
}
