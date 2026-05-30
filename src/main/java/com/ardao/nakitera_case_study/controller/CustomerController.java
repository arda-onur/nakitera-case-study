package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.response.CustomerResponse;
import com.ardao.nakitera_case_study.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/customer")
@Tag(name = "Customer", description = "Customer management endpoints")
public class CustomerController {
         private final CustomerService customerService;
         private final MessageSource messageSource;

    public CustomerController(CustomerService customerService, MessageSource messageSource) {
        this.customerService = customerService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Create customer", description = "Admin-only endpoint that creates a new customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
     @PostMapping("/create")
    public ResponseEntity<CustomerResponse> createCustomer(Locale locale){
        Customer customer = this.customerService.createCustomer();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CustomerResponse(customer.getId(),
                        this.messageSource.getMessage("customer.response.created",
                                null,  locale)));
    }
}
