package com.lima.paymentservice.domain.payment.service;

import com.lima.paymentservice.config.kafka.KafkaProducerService;
import com.lima.paymentservice.domain.payment.model.dto.PaymentCreateRequest;
import com.lima.paymentservice.domain.payment.model.dto.PaymentEventDto;
import com.lima.paymentservice.domain.payment.model.dto.PaymentResponse;
import com.lima.paymentservice.domain.payment.model.entity.Payment;
import com.lima.paymentservice.domain.payment.model.type.PaymentStatus;
import com.lima.paymentservice.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final KafkaProducerService kafkaProducerService;

  @Transactional
  @Retryable(
      retryFor = RuntimeException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  public PaymentResponse processPayment(PaymentCreateRequest request) {
    log.info("결제 처리 시작: orderId={}, amount={}, method={}",
        request.getOrderId(), request.getAmount(), request.getMethod());

    Payment payment = Payment.builder()
        .orderId(request.getOrderId())
        .amount(request.getAmount())
        .method(request.getMethod())
        .status(PaymentStatus.PENDING)
        .build();

    Payment savedPayment = paymentRepository.save(payment);

    // 외부 결제 API 호출 시뮬레이션
    String transactionId = simulateExternalPayment(savedPayment);

    savedPayment.complete(transactionId);
    Payment completedPayment = paymentRepository.save(savedPayment);

    log.info("결제 처리 완료: paymentId={}, orderId={}, transactionId={}",
        completedPayment.getId(), completedPayment.getOrderId(), transactionId);

    kafkaProducerService.sendPaymentEvent(PaymentEventDto.createCompleted(completedPayment));

    return PaymentResponse.from(completedPayment);
  }

  @Recover
  @Transactional
  public PaymentResponse recoverPayment(RuntimeException e, PaymentCreateRequest request) {
    log.error("결제 처리 최종 실패 (재시도 소진): orderId={}, reason={}",
        request.getOrderId(), e.getMessage());

    Payment payment = Payment.builder()
        .orderId(request.getOrderId())
        .amount(request.getAmount())
        .method(request.getMethod())
        .status(PaymentStatus.FAILED)
        .failureReason(e.getMessage())
        .build();

    Payment failedPayment = paymentRepository.save(payment);

    kafkaProducerService.sendPaymentEvent(PaymentEventDto.createFailed(failedPayment));

    return PaymentResponse.from(failedPayment);
  }

  @Transactional
  public PaymentResponse refundPayment(Long paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + paymentId));

    if (payment.getStatus() != PaymentStatus.COMPLETED) {
      throw new IllegalStateException("완료된 결제만 환불할 수 있습니다. 현재 상태: " + payment.getStatus());
    }

    payment.refund();
    Payment refundedPayment = paymentRepository.save(payment);

    log.info("환불 처리 완료: paymentId={}, orderId={}", paymentId, refundedPayment.getOrderId());

    kafkaProducerService.sendPaymentEvent(PaymentEventDto.createRefunded(refundedPayment));

    return PaymentResponse.from(refundedPayment);
  }

  public PaymentResponse getPayment(Long paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + paymentId));
    return PaymentResponse.from(payment);
  }

  public PaymentResponse getPaymentByOrderId(Long orderId) {
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new IllegalArgumentException("해당 주문의 결제를 찾을 수 없습니다: " + orderId));
    return PaymentResponse.from(payment);
  }

  /**
   * 외부 결제 API 호출 시뮬레이션
   */
  private String simulateExternalPayment(Payment payment) {
    log.debug("외부 결제 API 호출 시뮬레이션: orderId={}, amount={}",
        payment.getOrderId(), payment.getAmount());

    // 실제 환경에서는 PG사 API를 호출
    return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
