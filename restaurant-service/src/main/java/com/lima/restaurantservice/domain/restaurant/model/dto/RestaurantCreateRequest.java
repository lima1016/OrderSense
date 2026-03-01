package com.lima.restaurantservice.domain.restaurant.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantCreateRequest {

    @NotBlank(message = "가맹점 이름은 필수입니다")
    private String name;

    private String address;

    @NotBlank(message = "지역은 필수입니다")
    private String region;

    private String phone;

    private String category;
}
