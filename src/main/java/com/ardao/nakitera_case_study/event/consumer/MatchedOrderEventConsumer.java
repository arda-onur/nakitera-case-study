package com.ardao.nakitera_case_study.event.consumer;

import com.ardao.nakitera_case_study.event.dto.MatchedOrderEvent;
import com.ardao.nakitera_case_study.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MatchedOrderEventConsumer {
    private final AssetService assetService;

    public MatchedOrderEventConsumer(AssetService assetService) {
        this.assetService = assetService;
    }

    @KafkaListener(topics = "MATCHED_ORDER_EVENT", groupId = "matchedorder-consumer-group")
    public void consumeMatchedOrderEvent(MatchedOrderEvent matchedOrderEvent) {
        log.info("Consumed MATCHED_ORDER_EVENT. outboxId={}", matchedOrderEvent.outboxId());
         this.assetService.handleMatchedOrderEvent(matchedOrderEvent);
    }
}
