package com.ardao.nakitera_case_study.event.publisher;

import com.ardao.nakitera_case_study.entity.Outbox;
import com.ardao.nakitera_case_study.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEventPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Outbox> kafkaTemplate;

    public OrderEventPublisher(OutboxRepository outboxRepository, KafkaTemplate<String, Outbox> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents(){
         List<Outbox> unpublishedEvents = this.outboxRepository.findByPublishedFalse();

         for(Outbox unpublishedEvent : unpublishedEvents){
             kafkaTemplate.send("ORDER_EVENT", unpublishedEvent);
             unpublishedEvent.setPublished(true);
         }
    }

}
