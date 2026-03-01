package com.lima.restaurantservice.domain.restaurant.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantEventDto {

    private String eventType;
    private Long restaurantId;
    private Long orderId;
    private String region;
    private Integer prepTimeMinutes;
    private LocalDateTime timestamp;

    public static RestaurantEventDto orderAccepted(Long restaurantId, Long orderId, String region, Integer prepTimeMinutes) {
        return RestaurantEventDto.builder()
                .eventType("ORDER_ACCEPTED")
                .restaurantId(restaurantId)
                .orderId(orderId)
                .region(region)
                .prepTimeMinutes(prepTimeMinutes)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RestaurantEventDto orderRejected(Long restaurantId, Long orderId, String region) {
        return RestaurantEventDto.builder()
                .eventType("ORDER_REJECTED")
                .restaurantId(restaurantId)
                .orderId(orderId)
                .region(region)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RestaurantEventDto orderPreparing(Long restaurantId, Long orderId, String region) {
        return RestaurantEventDto.builder()
                .eventType("ORDER_PREPARING")
                .restaurantId(restaurantId)
                .orderId(orderId)
                .region(region)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RestaurantEventDto orderReady(Long restaurantId, Long orderId, String region) {
        return RestaurantEventDto.builder()
                .eventType("ORDER_READY")
                .restaurantId(restaurantId)
                .orderId(orderId)
                .region(region)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
