package com.ardao.nakitera_case_study.event.publisher;

import com.ardao.nakitera_case_study.entity.box.MatchedOrderOutbox;
import com.ardao.nakitera_case_study.entity.box.OrderOutbox;
import com.ardao.nakitera_case_study.event.dto.MatchedOrderEvent;
import com.ardao.nakitera_case_study.event.dto.OrderEvent;
import com.ardao.nakitera_case_study.repository.box.MatchedOrderOutboxRepository;
import com.ardao.nakitera_case_study.repository.box.OrderOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OrderEventPublisher {

    private final OrderOutboxRepository orderOutboxRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;



    public OrderEventPublisher(OrderOutboxRepository orderOutboxRepository, KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOrderEvents(){
        List<OrderOutbox> unpublishedEvents = this.orderOutboxRepository.findByPublishedAndEventTypeInOrderByIdAsc(
                false,
                List.of("ORDER_CREATED", "ORDER_CANCELED"));

        if(!unpublishedEvents.isEmpty()){
            for(OrderOutbox unpublishedEvent : unpublishedEvents){
                OrderEvent orderEvent = new OrderEvent(
                        unpublishedEvent.getId(),
                        unpublishedEvent.getOrderId(),
                        unpublishedEvent.getCustomerId(),
                        unpublishedEvent.getAssetName(),
                        unpublishedEvent.getOrderSide(),
                        unpublishedEvent.getSize(),
                        unpublishedEvent.getPrice(),
                        unpublishedEvent.getEventType());

                try {
                    log.info("Publishing ORDER_EVENT.outboxId={}",unpublishedEvent.getId());
                    kafkaTemplate.send("ORDER_EVENT", orderEvent).get();
                    unpublishedEvent.setPublished(true);
                    log.info("ORDER_EVENT published successfully. outboxId={}",
                            unpublishedEvent.getId());
                }catch (Exception e){
                    log.error("Could not send ORDER_EVENT outboxId={}",unpublishedEvent.getId());
                }
            }
        }
    }
}
