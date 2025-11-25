package com.lima.orderservice.domain.order.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

  @NotNull(message = "고객 ID는 필수입니다")
  private Long customerId;

  @NotNull(message = "가맹점 ID는 필수입니다")
  private Long restaurantId;

  @NotBlank(message = "배달 주소는 필수입니다")
  private String deliveryAddress;

  @NotNull(message = "배달 위도는 필수입니다")
  @DecimalMin(value = "-90.0", message = "위도는 -90 ~ 90 사이여야 합니다")
  @DecimalMax(value = "90.0", message = "위도는 -90 ~ 90 사이여야 합니다")
  private BigDecimal deliveryLat;

  @NotNull(message = "배달 경도는 필수입니다")
  @DecimalMin(value = "-180.0", message = "경도는 -180 ~ 180 사이여야 합니다")
  @DecimalMax(value = "180.0", message = "경도는 -180 ~ 180 사이여야 합니다")
  private BigDecimal deliveryLng;

  @NotBlank(message = "지역은 필수입니다")
  private String region;

  @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
  @Valid
  private List<OrderItemRequest> items;

  @NotBlank(message = "결제 수단은 필수입니다")
  private String paymentMethod;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderItemRequest {

    @NotBlank(message = "메뉴 이름은 필수입니다")
    private String menuItemName;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;

    @NotNull(message = "단가는 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "단가는 0보다 커야 합니다")
    private BigDecimal unitPrice;
  }
}
