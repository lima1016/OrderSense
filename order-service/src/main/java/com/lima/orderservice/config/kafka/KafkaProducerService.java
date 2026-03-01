package com.lima.orderservice.config.kafka;


import com.lima.orderservice.domain.order.model.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * 주문 이벤트를 Kafka로 발행
   */
  public void sendOrderEvent(OrderEventDto event) {
    String key = event.getOrderId().toString();

    CompletableFuture<SendResult<String, Object>> future =
        kafkaTemplate.send(KafkaConfig.ORDERS_TOPIC, key, event);

    future.whenComplete((result, ex) -> {
      if (ex == null) {
        log.info("Order event sent successfully: eventType={}, orderId={}, partition={}, offset={}",
            event.getEventType(),
            event.getOrderId(),
            result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
      } else {
        log.error("Failed to send order event: eventType={}, orderId={}",
            event.getEventType(),
            event.getOrderId(),
            ex);
      }
    });
  }

  /**
   * 배달 이벤트를 Kafka로 발행
   */
  public void sendDeliveryEvent(Object event) {
    kafkaTemplate.send(KafkaConfig.DELIVERIES_TOPIC, event)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info("Delivery event sent successfully");
          } else {
            log.error("Failed to send delivery event", ex);
          }
        });
  }

  /**
   * 결제 이벤트를 Kafka로 발행
   */
  public void sendPaymentEvent(Object event) {
    kafkaTemplate.send(KafkaConfig.PAYMENTS_TOPIC, event)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info("Payment event sent successfully");
          } else {
            log.error("Failed to send payment event", ex);
          }
        });
  }
}