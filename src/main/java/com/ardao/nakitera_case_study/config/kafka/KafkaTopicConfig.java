package com.ardao.nakitera_case_study.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
  private static final String ORDER_EVENT = "ORDER_EVENT";
  private static final String ASSET_EVENT = "ASSET_EVENT";
  private static final String MATCHED_ORDER_EVENT = "MATCHED_ORDER_EVENT";

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENT)
                .partitions(1)
                .build();
    }

    @Bean
    public NewTopic matchedOrderEventsTopic() {
        return TopicBuilder.name(MATCHED_ORDER_EVENT)
                .partitions(1)
                .build();
    }

    @Bean
    public NewTopic assetEventsTopic() {
        return TopicBuilder.name(ASSET_EVENT)
                .partitions(1)
                .build();
    }
}
