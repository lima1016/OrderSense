package com.lima.paymentservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

  public static final String PAYMENTS_TOPIC = "payments";

  @Bean
  public NewTopic paymentsTopic() {
    return TopicBuilder
        .name(PAYMENTS_TOPIC)
        .partitions(3)
        .replicas(1)
        .build();
  }
}
