package com.ardao.nakitera_case_study.model;

import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.enums.Side;
import lombok.Getter;

import java.util.Comparator;
import java.util.PriorityQueue;

@Getter
public class OrderBook {
    private final PriorityQueue<Order> buyQueue;
    private final PriorityQueue<Order> sellQueue;

    public OrderBook(){
        this.buyQueue = new PriorityQueue<>(Comparator.comparing(Order::getPrice).reversed());
        this.sellQueue = new PriorityQueue<>(Comparator.comparing(Order::getPrice));
    }

    public void addOrder(Order order) {
        if (order.getOrderSide() == Side.BUY) {
            buyQueue.offer(order);
        } else {
            sellQueue.offer(order);
        }
    }

}
