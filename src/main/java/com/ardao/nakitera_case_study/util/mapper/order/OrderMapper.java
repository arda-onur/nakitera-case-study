package com.ardao.nakitera_case_study.util.mapper.order;

import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.enums.Side;
import com.ardao.nakitera_case_study.exception.custom.InvalidOrderSideException;
import com.ardao.nakitera_case_study.request.order.OrderRequest;
import com.ardao.nakitera_case_study.response.OrderListResponse;

public class OrderMapper {

    public static Order toEntity(OrderRequest orderRequest) {
        Order order = new Order();

        order.setAssetName(orderRequest.assetName());
        order.setOrderSide(parseOrderSide(orderRequest.orderSide()));
        order.setSize(orderRequest.size());
        order.setPrice(orderRequest.price());

        return order;
    }

    public static OrderListResponse toOrderListResponse(Order order) {
        return new OrderListResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getAssetName(),
                order.getSize(),
                order.getPrice(),
                order.getOrderSide(),
                order.getOrderStatus(),
                order.getCreateDate()
        );

    }

    private static Side parseOrderSide(String orderSide) {
        try {
            return Side.valueOf(orderSide);
        } catch (IllegalArgumentException ex) {
            throw new InvalidOrderSideException("invalid.order.side.exception", orderSide);
        }
    }
    private OrderMapper() {

        }
    }
