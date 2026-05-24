package com.ardao.nakitera_case_study.service;

import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.model.CustomerModel;

public interface OrderService {
   void createOrder(Order order, CustomerModel customerModel);
}
