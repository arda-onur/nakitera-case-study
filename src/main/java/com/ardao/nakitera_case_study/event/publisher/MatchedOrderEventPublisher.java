package com.ardao.nakitera_case_study.event.publisher;


import com.ardao.nakitera_case_study.entity.box.MatchedOrderOutbox;
import com.ardao.nakitera_case_study.event.dto.MatchedOrderEvent;
import com.ardao.nakitera_case_study.repository.box.MatchedOrderOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MatchedOrderEventPublisher {

    private final MatchedOrderOutboxRepository matchedOrderOutboxRepository;
    private final KafkaTemplate<String, MatchedOrderEvent> matchedOrderEventKafkaTemplate;

    public MatchedOrderEventPublisher(MatchedOrderOutboxRepository matchedOrderOutboxRepository, KafkaTemplate<String, MatchedOrderEvent> matchedOrderEventKafkaTemplate) {
        this.matchedOrderOutboxRepository = matchedOrderOutboxRepository;
        this.matchedOrderEventKafkaTemplate = matchedOrderEventKafkaTemplate;
    }
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishMatchedOrdersEvents() {
        List<MatchedOrderOutbox> unpublishedEvents = this.matchedOrderOutboxRepository.findByPublished(false);

        if (!unpublishedEvents.isEmpty()) {
            log.info("Publishing MATCHED_ORDER_EVENT");

            for (MatchedOrderOutbox unpublishedEvent : unpublishedEvents) {
                MatchedOrderEvent matchedOrderEvent = new MatchedOrderEvent(
                        unpublishedEvent.getId(),
                        unpublishedEvent.getBuyOrderId(),
                        unpublishedEvent.getSellOrderId(),
                        unpublishedEvent.getBuyCustomerId(),
                        unpublishedEvent.getSellCustomerId(),
                        unpublishedEvent.getAssetName(),
                        unpublishedEvent.getMatchedSize(),
                        unpublishedEvent.getBuyPrice(),
                        unpublishedEvent.getSellPrice()
                );

                try {
                    log.info("Publishing MATCHED_ORDER_EVENT. outboxId={}", unpublishedEvent.getId());

                    matchedOrderEventKafkaTemplate.send("MATCHED_ORDER_EVENT", matchedOrderEvent).get();
                    unpublishedEvent.setPublished(true);

                    log.info("MATCHED_ORDER_EVENT published successfully. outboxId={}", unpublishedEvent.getId());

                } catch (Exception e) {
                    log.error("Could not send MATCHED_ORDER_EVENT. outboxId={}", unpublishedEvent.getId(), e);
                }
            }
        }
    }
}
