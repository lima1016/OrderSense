package com.lima.restaurantservice.domain.restaurant.model.dto;

import com.lima.restaurantservice.domain.restaurant.model.entity.Restaurant;
import com.lima.restaurantservice.domain.restaurant.model.type.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {

    private Long id;
    private String name;
    private String address;
    private String region;
    private String phone;
    private String category;
    private RestaurantStatus status;
    private Integer avgPrepTime;
    private Integer displayRank;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .region(restaurant.getRegion())
                .phone(restaurant.getPhone())
                .category(restaurant.getCategory())
                .status(restaurant.getStatus())
                .avgPrepTime(restaurant.getAvgPrepTime())
                .displayRank(restaurant.getDisplayRank())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}
