package com.lima.paymentservice.domain.payment.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateRequest {

  @NotNull(message = "주문 ID는 필수입니다")
  private Long orderId;

  @NotNull(message = "결제 금액은 필수입니다")
  private BigDecimal amount;

  @NotBlank(message = "결제 수단은 필수입니다")
  private String method;
}
