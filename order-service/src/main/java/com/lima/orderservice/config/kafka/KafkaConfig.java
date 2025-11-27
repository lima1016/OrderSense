package com.lima.orderservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

  public static final String ORDERS_TOPIC = "orders";
  public static final String DELIVERIES_TOPIC = "deliveries";
  public static final String PAYMENTS_TOPIC = "payments";

  @Bean
  public NewTopic ordersTopic() {
    return TopicBuilder
        .name(ORDERS_TOPIC)
        .partitions(3)  // 파티션 3개로 병렬 처리
        .replicas(1)    // 단일 브로커라서 1임
        .build();
  }

  @Bean
  public NewTopic deliveriesTopic() {
    return TopicBuilder
        .name(DELIVERIES_TOPIC)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic paymentsTopic() {
    return TopicBuilder
        .name(PAYMENTS_TOPIC)
        .partitions(3)
        .replicas(1)
        .build();
  }
}