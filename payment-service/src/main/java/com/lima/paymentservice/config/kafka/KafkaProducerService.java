package com.lima.paymentservice.config.kafka;

import com.lima.paymentservice.domain.payment.model.dto.PaymentEventDto;
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
   * 결제 이벤트를 Kafka로 발행
   */
  public void sendPaymentEvent(PaymentEventDto event) {
    String key = event.getOrderId().toString();

    CompletableFuture<SendResult<String, Object>> future =
        kafkaTemplate.send(KafkaConfig.PAYMENTS_TOPIC, key, event);

    future.whenComplete((result, ex) -> {
      if (ex == null) {
        log.info("결제 이벤트 발행 성공: eventType={}, paymentId={}, orderId={}, partition={}, offset={}",
            event.getEventType(),
            event.getPaymentId(),
            event.getOrderId(),
            result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
      } else {
        log.error("결제 이벤트 발행 실패: eventType={}, paymentId={}, orderId={}",
            event.getEventType(),
            event.getPaymentId(),
            event.getOrderId(),
            ex);
      }
    });
  }
}
