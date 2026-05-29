package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.response.CustomerResponse;
import com.ardao.nakitera_case_study.service.CustomerService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/customer")
public class CustomerController {
         private final CustomerService customerService;
         private final MessageSource messageSource;

    public CustomerController(CustomerService customerService, MessageSource messageSource) {
        this.customerService = customerService;
        this.messageSource = messageSource;
    }


     @PostMapping("/create")
    public ResponseEntity<CustomerResponse> createCustomer(Locale locale){
        Customer customer = this.customerService.createCustomer();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CustomerResponse(customer.getId(),
                        this.messageSource.getMessage("customer.response.created",
                                null,  locale)));
    }
}
