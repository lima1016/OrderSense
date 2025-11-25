package com.lima.orderservice.domain.order.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatsResponse {

  private String region;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Long totalOrders;
  private Long cancelledOrders;
  private Double cancellationRate;
  private Double averageDeliveryMinutes;
  private Map<String, Long> ordersByStatus;
}