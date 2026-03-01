package com.lima.paymentservice.config.kafka;

import com.lima.paymentservice.domain.payment.model.dto.PaymentCreateRequest;
import com.lima.paymentservice.domain.payment.model.entity.Payment;
import com.lima.paymentservice.domain.payment.repository.PaymentRepository;
import com.lima.paymentservice.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

  private final PaymentService paymentService;
  private final PaymentRepository paymentRepository;

  @KafkaListener(topics = "orders", groupId = "payment-service-group")
  public void handleOrderEvent(Map<String, Object> event) {
    try {
      String eventType = (String) event.get("eventType");
      Long orderId = ((Number) event.get("orderId")).longValue();

      log.info("주문 이벤트 수신: eventType={}, orderId={}", eventType, orderId);

      switch (eventType) {
        case "ORDER_CREATED" -> {
          BigDecimal totalAmount = new BigDecimal(event.get("totalAmount").toString());
          String paymentMethod = event.get("paymentMethod") != null
              ? (String) event.get("paymentMethod")
              : "CARD";

          PaymentCreateRequest request = PaymentCreateRequest.builder()
              .orderId(orderId)
              .amount(totalAmount)
              .method(paymentMethod)
              .build();

          paymentService.processPayment(request);
          log.info("주문 생성에 따른 결제 처리 완료: orderId={}", orderId);
        }
        case "ORDER_CANCELLED" -> {
          Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
          if (paymentOpt.isPresent()) {
            paymentService.refundPayment(paymentOpt.get().getId());
            log.info("주문 취소에 따른 환불 처리 완료: orderId={}", orderId);
          } else {
            log.warn("환불 처리할 결제 정보가 없습니다: orderId={}", orderId);
          }
        }
        default -> log.warn("알 수 없는 주문 이벤트 타입: {}", eventType);
      }
    } catch (Exception e) {
      log.error("주문 이벤트 처리 실패: {}", event, e);
    }
  }
}
