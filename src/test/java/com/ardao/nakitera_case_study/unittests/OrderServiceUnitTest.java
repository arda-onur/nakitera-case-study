package com.ardao.nakitera_case_study.unittests;

import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.entity.User;
import com.ardao.nakitera_case_study.entity.box.OrderOutbox;
import com.ardao.nakitera_case_study.enums.Side;
import com.ardao.nakitera_case_study.enums.Status;
import com.ardao.nakitera_case_study.exception.custom.CustomerNotFoundException;
import com.ardao.nakitera_case_study.exception.custom.OrderCannotBeCanceledException;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.repository.OrderRepository;
import com.ardao.nakitera_case_study.repository.box.MatchedOrderOutboxRepository;
import com.ardao.nakitera_case_study.repository.box.OrderOutboxRepository;
import com.ardao.nakitera_case_study.service.impl.OrderServiceImpl;
import com.ardao.nakitera_case_study.util.resolver.CustomerIdResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderOutboxRepository orderOutboxRepository;

    @Mock
    private MatchedOrderOutboxRepository matchedOrderOutboxRepository;

    @Mock
    private CustomerIdResolver customerIdResolver;

    @InjectMocks
    private OrderServiceImpl orderService;


    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_shouldSaveOrderAndCreateOutboxEvent() {
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setAssetName("THY");
        order.setOrderSide(Side.BUY);
        order.setSize(10);
        order.setPrice(100);

        when(customerIdResolver.resolveCustomerId(1L)).thenReturn(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(99L);
            return savedOrder;
        });

        orderService.createOrder(order, 1L);

        assertEquals(customer, order.getCustomer());
        assertNotNull(order.getCreateDate());

        verify(orderRepository).saveAndFlush(order);

        ArgumentCaptor<OrderOutbox> outboxCaptor = ArgumentCaptor.forClass(OrderOutbox.class);
        verify(orderOutboxRepository).save(outboxCaptor.capture());

        OrderOutbox savedOutbox = outboxCaptor.getValue();
        assertEquals(99L, savedOutbox.getOrderId());
        assertEquals(1L, savedOutbox.getCustomerId());
        assertEquals("ORDER_CREATED", savedOutbox.getEventType());
        assertEquals("THY", savedOutbox.getAssetName());
    }

    @Test
    void createOrder_shouldThrowWhenCustomerNotFound() {
        Order order = new Order();
        order.setAssetName("THY");
        order.setOrderSide(Side.BUY);
        order.setSize(10);
        order.setPrice(100);

        when(customerIdResolver.resolveCustomerId(1L)).thenReturn(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () ->orderService.createOrder(order,1L));

        verify(orderRepository, never()).saveAndFlush(any());
        verify(orderOutboxRepository, never()).save(any());
    }
     @Test
    void cancelOrder_shouldCancelOrderAndCreateOutboxEvent() {
         Customer customer = new Customer();
         customer.setId(1L);

         Order order = new Order();
         order.setId(10L);
         order.setCustomer(customer);
         order.setAssetName("THY");
         order.setOrderSide(Side.BUY);
         order.setSize(5);
         order.setPrice(100);
         order.setOrderStatus(Status.PENDING);

         Authentication authentication = new UsernamePasswordAuthenticationToken(
                 "admin",
                 null,
                 List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
         );

         SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
         securityContext.setAuthentication(authentication);
         SecurityContextHolder.setContext(securityContext);

         when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

         orderService.cancelOrder(10L);

         assertEquals(Status.CANCELED, order.getOrderStatus());


         ArgumentCaptor<OrderOutbox> outboxCaptor = ArgumentCaptor.forClass(OrderOutbox.class);
         verify(orderOutboxRepository).save(outboxCaptor.capture());

         OrderOutbox savedOutbox = outboxCaptor.getValue();
         assertEquals(10L, savedOutbox.getOrderId());
         assertEquals(1L, savedOutbox.getCustomerId());
         assertEquals("ORDER_CANCELED", savedOutbox.getEventType());
         assertEquals("THY", savedOutbox.getAssetName());
     }

     @Test
      void shouldThrowWhenOrderIsNotPending(){
         Customer customer = new Customer();
         customer.setId(1L);

         Order order = new Order();
         order.setId(10L);
         order.setCustomer(customer);
         order.setAssetName("THY");
         order.setOrderSide(Side.BUY);
         order.setSize(5);
         order.setPrice(100);
         order.setOrderStatus(Status.MATCHED);

         Authentication authentication = new UsernamePasswordAuthenticationToken(
                 "admin",
                 null,
                 List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
         );

         SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
         securityContext.setAuthentication(authentication);
         SecurityContextHolder.setContext(securityContext);

         when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

         assertThrows(OrderCannotBeCanceledException.class, () -> orderService.cancelOrder(10L));

         assertEquals(Status.MATCHED, order.getOrderStatus());
         verify(orderOutboxRepository, never()).save(any());
     }

     @Test
    void cancelOrder_shouldThrowWhenUserTriesToCancelAnotherUsersOrder(){
         Customer orderOwner = new Customer();
         orderOwner.setId(1L);

         Customer currentCustomer = new Customer();
         currentCustomer.setId(2L);

         User currentUser = new User();
         currentUser.setCustomer(currentCustomer);

         Order order = new Order();
         order.setId(10L);
         order.setCustomer(orderOwner);
         order.setAssetName("THY");
         order.setOrderSide(Side.BUY);
         order.setSize(5);
         order.setPrice(100);
         order.setOrderStatus(Status.PENDING);

         Authentication authentication = new UsernamePasswordAuthenticationToken(
                 currentUser,
                 null,
                 List.of(new SimpleGrantedAuthority("ROLE_USER"))
         );
         SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
         securityContext.setAuthentication(authentication);
         SecurityContextHolder.setContext(securityContext);

         when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

         assertThrows(AccessDeniedException.class, () -> orderService.cancelOrder(10L));

         assertEquals(Status.PENDING, order.getOrderStatus());
         verify(orderOutboxRepository, never()).save(any());
    }

    @Test
    void rejectOrder_shouldSetStatusRejectedWhenOrderIsPending(){
        Order order = new Order();
        order.setId(10L);
        order.setOrderStatus(Status.PENDING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        orderService.rejectOrder(10L);

        assertEquals(Status.REJECTED, order.getOrderStatus());
    }


    @Test
    void rejectOrder_shouldKeepStatusWhenOrderIsNotPending() {
        Order order = new Order();
        order.setId(10L);
        order.setOrderStatus(Status.MATCHED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        orderService.rejectOrder(10L);

        assertEquals(Status.MATCHED, order.getOrderStatus());

    }



    }
    
    



