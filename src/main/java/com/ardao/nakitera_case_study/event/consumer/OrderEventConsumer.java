package com.ardao.nakitera_case_study.event.consumer;

import com.ardao.nakitera_case_study.event.dto.OrderEvent;
import com.ardao.nakitera_case_study.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {
    private final AssetService assetService;

    public OrderEventConsumer(AssetService assetService) {
        this.assetService = assetService;
    }

    @KafkaListener(topics = "ORDER_EVENT", groupId = "order-consumer-group")
    public void consumeOrderEvent(OrderEvent orderEvent) {
        log.info("Consumed ORDER_EVENT. outboxId={}", orderEvent.outboxId());
        switch (orderEvent.eventType()) {
            case "ORDER_CREATED" -> {
                switch (orderEvent.orderSide()) {
                    case BUY -> this.assetService.handleOrderCreatedBuyEvent(orderEvent);
                    case SELL -> this.assetService.handleOrderCreatedSellEvent(orderEvent);
                }
            }
            case "ORDER_CANCELED" -> {
                switch (orderEvent.orderSide()) {
                    case BUY -> this.assetService.releaseReservedBuyOrder(orderEvent);
                    case SELL -> this.assetService.releaseReservedSellOrder(orderEvent);
                }
            }
        }

    }

    }

