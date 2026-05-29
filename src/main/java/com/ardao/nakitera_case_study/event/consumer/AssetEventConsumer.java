package com.ardao.nakitera_case_study.event.consumer;

import com.ardao.nakitera_case_study.event.dto.AssetEvent;
import com.ardao.nakitera_case_study.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class AssetEventConsumer {
    private final OrderService orderService;
    public static final String ASSET_RESERVATION_FAILED = "ASSET_RESERVATION_FAILED";

    public AssetEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "ASSET_EVENT", groupId = "asset-consumer-group")
    public void consumeAssetEvent(AssetEvent assetEvent){
        log.info("Consumed MATCHED_ORDER_EVENT. outboxId={}", assetEvent.outboxId());
         switch (assetEvent.eventType()){
             case ASSET_RESERVATION_FAILED -> this.orderService.rejectOrder(assetEvent.orderId());
         }

    }

}
