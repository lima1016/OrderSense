package com.lima.orderservice.domain.order.model.dto;

import com.lima.orderservice.domain.order.model.entity.Order;
import com.lima.orderservice.domain.order.model.type.OrderStatus;
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
public class OrderEventDto {

  private String eventType;  // ORDER_CREATED, ORDER_CANCELLED, ORDER_STATUS_CHANGED
  private Long orderId;
  private Long customerId;
  private Long restaurantId;
  private Long riderId;
  private String region;
  private OrderStatus status;
  private OrderStatus previousStatus;
  private BigDecimal totalAmount;
  private Integer itemCount;
  private String cancelReason;
  private LocalDateTime timestamp;

  public static OrderEventDto createOrderCreated(Order order) {
    return OrderEventDto.builder()
        .eventType("ORDER_CREATED")
        .orderId(order.getId())
        .customerId(order.getCustomer().getId())
        .restaurantId(order.getRestaurantId())
        .region(order.getRegion())
        .status(order.getStatus())
        .totalAmount(order.getTotalAmount())
        .itemCount(order.getItems().size())
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static OrderEventDto createOrderCancelled(Order order) {
    return OrderEventDto.builder()
        .eventType("ORDER_CANCELLED")
        .orderId(order.getId())
        .customerId(order.getCustomer().getId())
        .restaurantId(order.getRestaurantId())
        .region(order.getRegion())
        .status(OrderStatus.CANCELLED)
        .cancelReason(order.getCancelReason())
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static OrderEventDto createOrderStatusChanged(Order order, OrderStatus previousStatus) {
    return OrderEventDto.builder()
        .eventType("ORDER_STATUS_CHANGED")
        .orderId(order.getId())
        .customerId(order.getCustomer().getId())
        .restaurantId(order.getRestaurantId())
        .riderId(order.getRiderId())
        .region(order.getRegion())
        .status(order.getStatus())
        .previousStatus(previousStatus)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
