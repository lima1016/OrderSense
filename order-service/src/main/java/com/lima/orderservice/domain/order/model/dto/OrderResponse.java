package com.lima.orderservice.domain.order.model.dto;

import com.lima.orderservice.domain.order.model.entity.Order;
import com.lima.orderservice.domain.order.model.entity.OrderItem;
import com.lima.orderservice.domain.order.model.type.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

  private Long orderId;
  private Long customerId;
  private String customerName;
  private Long restaurantId;
  private Long riderId;
  private OrderStatus status;
  private BigDecimal totalAmount;
  private String deliveryAddress;
  private BigDecimal deliveryLat;
  private BigDecimal deliveryLng;
  private String region;
  private LocalDateTime orderTime;
  private LocalDateTime acceptedTime;
  private LocalDateTime prepStartTime;
  private LocalDateTime readyTime;
  private LocalDateTime pickupTime;
  private LocalDateTime deliveredTime;
  private LocalDateTime cancelledTime;
  private String cancelReason;
  private Integer estimatedDeliveryMinutes;
  private Integer actualDeliveryMinutes;
  private List<OrderItemResponse> items;
  private LocalDateTime createdAt;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderItemResponse {
    private Long id;
    private String menuItemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public static OrderItemResponse from(OrderItem item) {
      return OrderItemResponse.builder()
          .id(item.getId())
          .menuItemName(item.getMenuItemName())
          .quantity(item.getQuantity())
          .unitPrice(item.getUnitPrice())
          .totalPrice(item.getTotalPrice())
          .build();
    }
  }

  public static OrderResponse from(Order order) {
    return OrderResponse.builder()
        .orderId(order.getId())
        .customerId(order.getCustomer().getId())
        .customerName(order.getCustomer().getName())
        .restaurantId(order.getRestaurantId())
        .riderId(order.getRiderId())
        .status(order.getStatus())
        .totalAmount(order.getTotalAmount())
        .deliveryAddress(order.getDeliveryAddress())
        .deliveryLat(order.getDeliveryLat())
        .deliveryLng(order.getDeliveryLng())
        .region(order.getRegion())
        .orderTime(order.getOrderTime())
        .acceptedTime(order.getAcceptedTime())
        .prepStartTime(order.getPrepStartTime())
        .readyTime(order.getReadyTime())
        .pickupTime(order.getPickupTime())
        .deliveredTime(order.getDeliveredTime())
        .cancelledTime(order.getCancelledTime())
        .cancelReason(order.getCancelReason())
        .estimatedDeliveryMinutes(order.getEstimatedDeliveryMinutes())
        .actualDeliveryMinutes(order.getActualDeliveryMinutes())
        .items(order.getItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList()))
        .createdAt(order.getCreatedAt())
        .build();
  }
}
