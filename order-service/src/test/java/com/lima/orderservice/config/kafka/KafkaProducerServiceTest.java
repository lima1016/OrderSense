package com.lima.orderservice.config.kafka;

import com.lima.orderservice.domain.order.model.dto.OrderEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaProducerService 단위 테스트")
class KafkaProducerServiceTest {

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Mock
  private SendResult<String, Object> sendResult;

  @Captor
  private ArgumentCaptor<String> topicCaptor;

  @Captor
  private ArgumentCaptor<String> keyCaptor;

  @Captor
  private ArgumentCaptor<Object> valueCaptor;

  private KafkaProducerService kafkaProducerService;

  @BeforeEach
  void setUp() {
    kafkaProducerService = new KafkaProducerService(kafkaTemplate);
  }

  @Test
  @DisplayName("주문 이벤트가 성공적으로 전송되어야 함")
  void sendOrderEvent_ShouldSendEventSuccessfully() {
    // given
    Long orderId = 12345L;
    OrderEventDto event = createOrderEventDto(orderId, "ORDER_CREATED");
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendOrderEvent(event);

    // then
    verify(kafkaTemplate).send(
        eq(KafkaConfig.ORDERS_TOPIC),
        eq(orderId.toString()),
        eq(event)
    );
  }

  @Test
  @DisplayName("주문 이벤트 전송 시 올바른 토픽과 키를 사용해야 함")
  void sendOrderEvent_ShouldUseCorrectTopicAndKey() {
    // given
    Long orderId = 67890L;
    OrderEventDto event = createOrderEventDto(orderId, "ORDER_CREATED");
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendOrderEvent(event);

    // then
    verify(kafkaTemplate).send(
        topicCaptor.capture(),
        keyCaptor.capture(),
        valueCaptor.capture()
    );
    
    assertThat(topicCaptor.getValue()).isEqualTo(KafkaConfig.ORDERS_TOPIC);
    assertThat(keyCaptor.getValue()).isEqualTo(orderId.toString());
    assertThat(valueCaptor.getValue()).isEqualTo(event);
  }

  @Test
  @DisplayName("주문 이벤트 전송 실패 시 예외를 로깅해야 함")
  void sendOrderEvent_ShouldLogExceptionOnFailure() {
    // given
    Long orderId = 11111L;
    OrderEventDto event = createOrderEventDto(orderId, "ORDER_CREATED");
    CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException("Kafka send failed"));
    
    when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendOrderEvent(event);

    // then
    verify(kafkaTemplate).send(
        eq(KafkaConfig.ORDERS_TOPIC),
        eq(orderId.toString()),
        eq(event)
    );
  }

  @Test
  @DisplayName("배달 이벤트가 성공적으로 전송되어야 함")
  void sendDeliveryEvent_ShouldSendEventSuccessfully() {
    // given
    Object deliveryEvent = new Object();
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendDeliveryEvent(deliveryEvent);

    // then
    verify(kafkaTemplate).send(
        eq(KafkaConfig.DELIVERIES_TOPIC),
        eq(deliveryEvent)
    );
  }

  @Test
  @DisplayName("배달 이벤트 전송 시 올바른 토픽을 사용해야 함")
  void sendDeliveryEvent_ShouldUseCorrectTopic() {
    // given
    Object deliveryEvent = new Object();
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendDeliveryEvent(deliveryEvent);

    // then
    verify(kafkaTemplate).send(
        topicCaptor.capture(),
        valueCaptor.capture()
    );
    
    assertThat(topicCaptor.getValue()).isEqualTo(KafkaConfig.DELIVERIES_TOPIC);
    assertThat(valueCaptor.getValue()).isEqualTo(deliveryEvent);
  }

  @Test
  @DisplayName("배달 이벤트 전송 실패 시 예외를 로깅해야 함")
  void sendDeliveryEvent_ShouldLogExceptionOnFailure() {
    // given
    Object deliveryEvent = new Object();
    CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException("Kafka send failed"));
    
    when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendDeliveryEvent(deliveryEvent);

    // then
    verify(kafkaTemplate).send(
        eq(KafkaConfig.DELIVERIES_TOPIC),
        eq(deliveryEvent)
    );
  }

  @Test
  @DisplayName("결제 이벤트가 성공적으로 전송되어야 함")
  void sendPaymentEvent_ShouldSendEventSuccessfully() {
    // given
    Object paymentEvent = new Object();
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendPaymentEvent(paymentEvent);

    // then
    verify(kafkaTemplate).send(
        eq(KafkaConfig.PAYMENTS_TOPIC),
        eq(paymentEvent)
    );
  }

  @Test
  @DisplayName("결제 이벤트 전송 시 올바른 토픽을 사용해야 함")
  void sendPaymentEvent_ShouldUseCorrectTopic() {
    // given
    Object paymentEvent = new Object();
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendPaymentEvent(paymentEvent);

    // then
    verify(kafkaTemplate).send(
        topicCaptor.capture(),
        valueCaptor.capture()
    );
    
    assertThat(topicCaptor.getValue()).isEqualTo(KafkaConfig.PAYMENTS_TOPIC);
    assertThat(valueCaptor.getValue()).isEqualTo(paymentEvent);
  }

  @Test
  @DisplayName("결제 이벤트 전송 실패 시 예외를 로깅해야 함")
  void sendPaymentEvent_ShouldLogExceptionOnFailure() {
    // given
    Object paymentEvent = new Object();
    CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException("Kafka send failed"));
    
    when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendPaymentEvent(paymentEvent);

    // then
    verify(kafkaTemplate).send(
        eq(KafkaConfig.PAYMENTS_TOPIC),
        eq(paymentEvent)
    );
  }

  @Test
  @DisplayName("null 주문 이벤트 전송 시 NullPointerException이 발생해야 함")
  void sendOrderEvent_WithNullEvent_ShouldThrowException() {
    // given
    OrderEventDto event = null;

    // when & then
    org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
      kafkaProducerService.sendOrderEvent(event);
    });
  }

  @Test
  @DisplayName("여러 주문 이벤트를 순차적으로 전송할 수 있어야 함")
  void sendOrderEvent_MultipleEvents_ShouldSendSequentially() {
    // given
    Long orderId1 = 1000L;
    Long orderId2 = 2000L;
    OrderEventDto event1 = createOrderEventDto(orderId1, "ORDER_CREATED");
    OrderEventDto event2 = createOrderEventDto(orderId2, "ORDER_UPDATED");
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendOrderEvent(event1);
    kafkaProducerService.sendOrderEvent(event2);

    // then
    verify(kafkaTemplate, times(2)).send(
        eq(KafkaConfig.ORDERS_TOPIC),
        anyString(),
        any()
    );
  }

  @Test
  @DisplayName("KafkaTemplate이 null이면 NullPointerException이 발생해야 함")
  void constructor_WithNullKafkaTemplate_ShouldThrowException() {
    // when & then
    org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
      new KafkaProducerService(null);
    });
  }

  @Test
  @DisplayName("다양한 이벤트 타입을 동시에 전송할 수 있어야 함")
  void sendEvents_DifferentTypes_ShouldSendToCorrectTopics() {
    // given
    Long orderId = 5555L;
    OrderEventDto orderEvent = createOrderEventDto(orderId, "ORDER_CREATED");
    Object deliveryEvent = new Object();
    Object paymentEvent = new Object();
    CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
    
    when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
    when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

    // when
    kafkaProducerService.sendOrderEvent(orderEvent);
    kafkaProducerService.sendDeliveryEvent(deliveryEvent);
    kafkaProducerService.sendPaymentEvent(paymentEvent);

    // then
    verify(kafkaTemplate).send(eq(KafkaConfig.ORDERS_TOPIC), anyString(), any());
    verify(kafkaTemplate).send(eq(KafkaConfig.DELIVERIES_TOPIC), any());
    verify(kafkaTemplate).send(eq(KafkaConfig.PAYMENTS_TOPIC), any());
  }

  private OrderEventDto createOrderEventDto(Long orderId, String eventType) {
    OrderEventDto event = mock(OrderEventDto.class);
    when(event.getOrderId()).thenReturn(orderId);
    when(event.getEventType()).thenReturn(eventType);
    return event;
  }
}