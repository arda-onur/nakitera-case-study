package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.response.OrderListResponse;
import com.ardao.nakitera_case_study.util.mapper.order.OrderMapper;
import com.ardao.nakitera_case_study.request.order.OrderRequest;
import com.ardao.nakitera_case_study.response.OrderResponse;
import com.ardao.nakitera_case_study.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order", description = "Order management endpoints")
public class OrderController {
    private final OrderService orderService;
    private final MessageSource messageSource;

    public OrderController(OrderService orderService, MessageSource messageSource) {
        this.orderService = orderService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Create order", description = "Creates a new BUY or SELL order for a customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest, Locale locale){
        this.orderService.createOrder(OrderMapper.toEntity(orderRequest),orderRequest.customerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrderResponse(
                        this.messageSource.getMessage("order.response.created", null, locale)));

    }


    @Operation(summary = "Cancel order", description = "Cancels a pending order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order canceled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be canceled"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@Parameter(description = "Order id",
                                                                 required = true,
                                                                 example = "1")
                                                       @RequestParam long orderId, Locale locale) {
        this.orderService.cancelOrder(orderId);

        return ResponseEntity.ok(
                new OrderResponse(
                        this.messageSource.getMessage("order.response.canceled", null, locale)
                )
        );
    }
    @Operation(summary = "Match pending orders", description = "Admin-only endpoint that matches pending orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching completed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PatchMapping("/match")
    public ResponseEntity<OrderResponse> matchOrder(Locale locale) {
        this.orderService.matchOrders();

        return ResponseEntity.ok(
                new OrderResponse(
                        this.messageSource.getMessage("order.response.created", null, locale)
                )
        );
    }

    @Operation(summary = "List orders", description = "Returns orders for a customer within an optional date range.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/list")
    public ResponseEntity<Page<OrderListResponse>> getCustomerList(@Parameter(description = "Page number",
                                                                               example = "0")
                                                                       @RequestParam(defaultValue = "0") @PositiveOrZero
                                                                       int page,
                                                                   @Parameter(description = "Page size",
                                                                              example = "10")
                                                                       @RequestParam(defaultValue = "10") @Positive
                                                                       int size,
                                                                   @Parameter(description = "Customer id",
                                                                              example = "1")
                                                                       @RequestParam(required = false) Long customerId,
                                                                   @Parameter(description = "Start date",
                                                                              example = "2026-05-30")
                                                                       @RequestParam(required = false)
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                       LocalDate startDate,
                                                                   @Parameter(description = "End date",
                                                                              example = "2026-05-30")
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
