package com.ardao.nakitera_case_study.service;

import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.response.OrderListResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface OrderService {
   void createOrder(Order order, Long customerId);
   void cancelOrder(Long orderId);
   void rejectOrder(Long orderId);
   void matchOrders();
   Page<OrderListResponse> getCustomerOrders(int page, int size, Long customerId, LocalDate startDate, LocalDate endDate);
}
