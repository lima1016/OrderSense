package com.lima.actionexecutorservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String ANOMALIES_TOPIC = "anomalies";
    public static final String ACTIONS_TOPIC = "actions";

    @Bean
    public NewTopic anomaliesTopic() {
        return TopicBuilder.name(ANOMALIES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic actionsTopic() {
        return TopicBuilder.name(ACTIONS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
