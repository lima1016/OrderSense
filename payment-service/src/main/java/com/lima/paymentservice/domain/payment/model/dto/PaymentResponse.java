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
public class PaymentResponse {

  private Long paymentId;
  private Long orderId;
  private BigDecimal amount;
  private String method;
  private PaymentStatus status;
  private String transactionId;
  private String failureReason;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static PaymentResponse from(Payment payment) {
    return PaymentResponse.builder()
        .paymentId(payment.getId())
        .orderId(payment.getOrderId())
        .amount(payment.getAmount())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .transactionId(payment.getTransactionId())
        .failureReason(payment.getFailureReason())
        .createdAt(payment.getCreatedAt())
        .updatedAt(payment.getUpdatedAt())
        .build();
  }
}
