package com.lima.restaurantservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String ORDERS_TOPIC = "orders";
    public static final String RESTAURANTS_TOPIC = "restaurants";

    @Bean
    public NewTopic restaurantsTopic() {
        return TopicBuilder
                .name(RESTAURANTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
