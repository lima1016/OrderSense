package com.lima.paymentservice.domain.payment.model.dto;

import com.lima.paymentservice.domain.payment.model.entity.Payment;
import com.lima.paymentservice.domain.payment.model.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEventDto {

  private String eventType;  // PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_REFUNDED
  private Long paymentId;
  private Long orderId;
  private BigDecimal amount;
  private String method;
  private PaymentStatus status;
  private String transactionId;
  private String failureReason;
  private LocalDateTime timestamp;

  public static PaymentEventDto createCompleted(Payment payment) {
    return PaymentEventDto.builder()
        .eventType("PAYMENT_COMPLETED")
        .paymentId(payment.getId())
        .orderId(payment.getOrderId())
        .amount(payment.getAmount())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .transactionId(payment.getTransactionId())
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static PaymentEventDto createFailed(Payment payment) {
    return PaymentEventDto.builder()
        .eventType("PAYMENT_FAILED")
        .paymentId(payment.getId())
        .orderId(payment.getOrderId())
        .amount(payment.getAmount())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .failureReason(payment.getFailureReason())
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static PaymentEventDto createRefunded(Payment payment) {
    return PaymentEventDto.builder()
        .eventType("PAYMENT_REFUNDED")
        .paymentId(payment.getId())
        .orderId(payment.getOrderId())
        .amount(payment.getAmount())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .transactionId(payment.getTransactionId())
        .timestamp(LocalDateTime.now())
        .build();
  }
}
