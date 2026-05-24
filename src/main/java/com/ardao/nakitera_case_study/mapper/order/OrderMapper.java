package com.ardao.nakitera_case_study.mapper.order;

import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.enums.Side;
import com.ardao.nakitera_case_study.request.order.OrderRequest;

public class OrderMapper {

    public static Order toEntity(OrderRequest orderRequest){
        Order order = new Order();

        order.setAssetName(orderRequest.assetName());
        order.setOrderSide(Side.valueOf(orderRequest.orderSide()));
        order.setSize(orderRequest.size());
        order.setPrice(orderRequest.price());

        return order;
    }

    private OrderMapper(){

    }
}
