package com.ardao.nakitera_case_study.event.publisher;

import com.ardao.nakitera_case_study.entity.box.OrderOutbox;
import com.ardao.nakitera_case_study.event.dto.AssetEvent;
import com.ardao.nakitera_case_study.repository.box.OrderOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AssetEventPublisher {
    private final OrderOutboxRepository orderOutboxRepository;
    private final KafkaTemplate<String, AssetEvent> kafkaTemplate;

    public AssetEventPublisher(OrderOutboxRepository orderOutboxRepository, KafkaTemplate<String, AssetEvent> kafkaTemplate) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishAssetEvent() {
        List<OrderOutbox> unpublishedCanceledEvents = this.orderOutboxRepository.findByPublishedAndEventTypeInOrderByIdAsc(
                false,
                List.of("ASSET_RESERVATION_FAILED")
        );

        if (!unpublishedCanceledEvents.isEmpty()) {
            log.info("Publishing ASSET_EVENT");

            for (OrderOutbox unpublishedEvent : unpublishedCanceledEvents) {
                AssetEvent assetEvent = new AssetEvent(
                        unpublishedEvent.getId(),
                        unpublishedEvent.getOrderId(),
                        unpublishedEvent.getCustomerId(),
                        unpublishedEvent.getAssetName(),
                        unpublishedEvent.getEventType(),
                        unpublishedEvent.getOrderSide(),
                        unpublishedEvent.getSize(),
                        unpublishedEvent.getPrice()
                );

                try {
                    log.info("Publishing ASSET_EVENT outboxId={}",unpublishedEvent.getId());

                    kafkaTemplate.send("ASSET_EVENT", assetEvent).get();
                    unpublishedEvent.setPublished(true);

                    log.info("ASSET_EVENT published successfully. outboxId={}, orderId={}, eventType={}",
                            unpublishedEvent.getId());

                } catch (Exception e) {
                    log.error("Could not send ASSET_EVENT. outboxId={}", unpublishedEvent.getId());
                }
            }
        }
    }
}
