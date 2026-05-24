package com.ardao.nakitera_case_study.service.impl;

import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.entity.Outbox;
import com.ardao.nakitera_case_study.entity.User;
import com.ardao.nakitera_case_study.exception.custom.CustomerNotFoundException;
import com.ardao.nakitera_case_study.model.CustomerModel;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.repository.OrderRepository;
import com.ardao.nakitera_case_study.repository.OutboxRepository;
import com.ardao.nakitera_case_study.service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    public OrderServiceImpl(CustomerRepository customerRepository, OrderRepository orderRepository, OutboxRepository outboxRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
    }


    @Override
    @Transactional
    public void createOrder(Order order, CustomerModel customerModel) {
        long id = resolveCustomerId(customerModel);
        Customer customer = this.customerRepository.findById(id)
                              .orElseThrow(() -> new CustomerNotFoundException("customer.not.found.exception", id));

        order.setCustomer(customer);
        order.setCreateDate(Instant.now());

        this.orderRepository.save(order);

        Outbox outboxEvent = createOutboxEvent(customer,order);

      this.outboxRepository.save(outboxEvent);

    }


    private long resolveCustomerId(CustomerModel customerModel){
       Authentication authentication = Objects.requireNonNull(
                                            SecurityContextHolder.getContext().getAuthentication());

        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if(isAdmin){
            if (customerModel.getId() == null)
                throw new IllegalArgumentException("customer.id.required.exception");
            return customerModel.getId();
        }
        User currentUser = (User)authentication.getPrincipal();

        return currentUser.getCustomer().getId();
    }

    private Outbox createOutboxEvent(Customer customer, Order order){
        Outbox outboxEvent = new Outbox();
        outboxEvent.setCustomerId(customer.getId());
        outboxEvent.setEventType("ORDER-CREATED");
        outboxEvent.setAssetName(order.getAssetName());
        outboxEvent.setOrderSide(order.getOrderSide());
        outboxEvent.setPrice(order.getPrice());
        outboxEvent.setSize(order.getSize());
        return outboxEvent;
    }
}
