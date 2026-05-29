package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.response.OrderListResponse;
import com.ardao.nakitera_case_study.util.mapper.order.OrderMapper;
import com.ardao.nakitera_case_study.request.order.OrderRequest;
import com.ardao.nakitera_case_study.response.OrderResponse;
import com.ardao.nakitera_case_study.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Locale;

@RestController
@Validated
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final MessageSource messageSource;

    public OrderController(OrderService orderService, MessageSource messageSource) {
        this.orderService = orderService;
        this.messageSource = messageSource;
    }
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest, Locale locale){
        this.orderService.createOrder(OrderMapper.toEntity(orderRequest),orderRequest.customerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrderResponse(
                        this.messageSource.getMessage("order.response.created", null, locale)));

    }

    @PatchMapping("/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@RequestParam long orderId, Locale locale) {
        this.orderService.cancelOrder(orderId);

        return ResponseEntity.ok(
                new OrderResponse(
                        this.messageSource.getMessage("order.response.canceled", null, locale)
                )
        );
    }

    @PatchMapping("/match")
    public ResponseEntity<OrderResponse> matchOrder(Locale locale) {
        this.orderService.matchOrders();

        return ResponseEntity.ok(
                new OrderResponse(
                        this.messageSource.getMessage("order.response.created", null, locale)
                )
        );
    }
    @GetMapping("/list")
    public ResponseEntity<Page<OrderListResponse>> getCustomerList(@RequestParam(defaultValue = "0")
                                                                   @PositiveOrZero
                                                                   int page,
                                                                   @RequestParam(defaultValue = "10")
                                                                   @Positive
                                                                   int size,
                                                                   @RequestParam(required = false)
                                                                   Long customerId,
                                                                   @RequestParam(required = false)
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                   LocalDate startDate,
                                                                   @RequestParam(required = false)
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                   LocalDate endDate){
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.orderService.getCustomerOrders(page,
                        size,
                        customerId,
                        startDate,
                        endDate));
    }

}
