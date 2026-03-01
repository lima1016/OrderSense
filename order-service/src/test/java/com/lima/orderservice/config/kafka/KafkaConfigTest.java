package com.lima.orderservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaConfig 단위 테스트")
class KafkaConfigTest {

  private KafkaConfig kafkaConfig;

  @BeforeEach
  void setUp() {
    kafkaConfig = new KafkaConfig();
  }

  @Test
  @DisplayName("ordersTopic 빈이 올바르게 생성되어야 함")
  void ordersTopic_ShouldCreateTopicWithCorrectConfiguration() {
    // when
    NewTopic topic = kafkaConfig.ordersTopic();

    // then
    assertThat(topic).isNotNull();
    assertThat(topic.name()).isEqualTo(KafkaConfig.ORDERS_TOPIC);
    assertThat(topic.name()).isEqualTo("orders");
    assertThat(topic.numPartitions()).isEqualTo(3);
    assertThat(topic.replicationFactor()).isEqualTo((short) 1);
  }

  @Test
  @DisplayName("deliveriesTopic 빈이 올바르게 생성되어야 함")
  void deliveriesTopic_ShouldCreateTopicWithCorrectConfiguration() {
    // when
    NewTopic topic = kafkaConfig.deliveriesTopic();

    // then
    assertThat(topic).isNotNull();
    assertThat(topic.name()).isEqualTo(KafkaConfig.DELIVERIES_TOPIC);
    assertThat(topic.name()).isEqualTo("deliveries");
    assertThat(topic.numPartitions()).isEqualTo(3);
    assertThat(topic.replicationFactor()).isEqualTo((short) 1);
  }

  @Test
  @DisplayName("paymentsTopic 빈이 올바르게 생성되어야 함")
  void paymentsTopic_ShouldCreateTopicWithCorrectConfiguration() {
    // when
    NewTopic topic = kafkaConfig.paymentsTopic();

    // then
    assertThat(topic).isNotNull();
    assertThat(topic.name()).isEqualTo(KafkaConfig.PAYMENTS_TOPIC);
    assertThat(topic.name()).isEqualTo("payments");
    assertThat(topic.numPartitions()).isEqualTo(3);
    assertThat(topic.replicationFactor()).isEqualTo((short) 1);
  }

  @Test
  @DisplayName("모든 토픽은 서로 다른 이름을 가져야 함")
  void allTopics_ShouldHaveUniqueName() {
    // when
    NewTopic ordersTopic = kafkaConfig.ordersTopic();
    NewTopic deliveriesTopic = kafkaConfig.deliveriesTopic();
    NewTopic paymentsTopic = kafkaConfig.paymentsTopic();

    // then
    assertThat(ordersTopic.name()).isNotEqualTo(deliveriesTopic.name());
    assertThat(ordersTopic.name()).isNotEqualTo(paymentsTopic.name());
    assertThat(deliveriesTopic.name()).isNotEqualTo(paymentsTopic.name());
  }

  @Test
  @DisplayName("모든 토픽은 동일한 파티션 설정을 가져야 함")
  void allTopics_ShouldHaveSamePartitionConfiguration() {
    // when
    NewTopic ordersTopic = kafkaConfig.ordersTopic();
    NewTopic deliveriesTopic = kafkaConfig.deliveriesTopic();
    NewTopic paymentsTopic = kafkaConfig.paymentsTopic();

    // then
    assertThat(ordersTopic.numPartitions()).isEqualTo(3);
    assertThat(deliveriesTopic.numPartitions()).isEqualTo(3);
    assertThat(paymentsTopic.numPartitions()).isEqualTo(3);
  }

  @Test
  @DisplayName("모든 토픽은 동일한 복제 설정을 가져야 함")
  void allTopics_ShouldHaveSameReplicationConfiguration() {
    // when
    NewTopic ordersTopic = kafkaConfig.ordersTopic();
    NewTopic deliveriesTopic = kafkaConfig.deliveriesTopic();
    NewTopic paymentsTopic = kafkaConfig.paymentsTopic();

    // then
    assertThat(ordersTopic.replicationFactor()).isEqualTo((short) 1);
    assertThat(deliveriesTopic.replicationFactor()).isEqualTo((short) 1);
    assertThat(paymentsTopic.replicationFactor()).isEqualTo((short) 1);
  }

  @Test
  @DisplayName("토픽 상수가 올바른 값을 가져야 함")
  void topicConstants_ShouldHaveCorrectValues() {
    // then
    assertThat(KafkaConfig.ORDERS_TOPIC).isEqualTo("orders");
    assertThat(KafkaConfig.DELIVERIES_TOPIC).isEqualTo("deliveries");
    assertThat(KafkaConfig.PAYMENTS_TOPIC).isEqualTo("payments");
  }

  @Test
  @DisplayName("동일한 토픽 빈을 여러 번 호출해도 같은 설정으로 생성되어야 함")
  void sameTopic_ShouldBeIdempotent() {
    // when
    NewTopic topic1 = kafkaConfig.ordersTopic();
    NewTopic topic2 = kafkaConfig.ordersTopic();

    // then
    assertThat(topic1.name()).isEqualTo(topic2.name());
    assertThat(topic1.numPartitions()).isEqualTo(topic2.numPartitions());
    assertThat(topic1.replicationFactor()).isEqualTo(topic2.replicationFactor());
  }

  @Test
  @DisplayName("토픽 빈은 null이 아닌 설정 맵을 가져야 함")
  void topics_ShouldHaveNonNullConfigMap() {
    // when
    NewTopic ordersTopic = kafkaConfig.ordersTopic();
    NewTopic deliveriesTopic = kafkaConfig.deliveriesTopic();
    NewTopic paymentsTopic = kafkaConfig.paymentsTopic();

    // then
    assertThat(ordersTopic.configs()).isNotNull();
    assertThat(deliveriesTopic.configs()).isNotNull();
    assertThat(paymentsTopic.configs()).isNotNull();
  }
}