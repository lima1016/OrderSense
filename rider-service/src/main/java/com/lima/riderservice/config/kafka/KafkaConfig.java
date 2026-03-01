package com.lima.riderservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String DELIVERIES_TOPIC = "deliveries";
    public static final String RIDERS_TOPIC = "riders";

    @Bean
    public NewTopic deliveriesTopic() {
        return TopicBuilder
                .name(DELIVERIES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ridersTopic() {
        return TopicBuilder
                .name(RIDERS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
