package com.lima.restaurantservice.domain.restaurant.model.dto;

import com.lima.restaurantservice.domain.restaurant.model.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private BigDecimal price;
    private Boolean available;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .restaurantId(menu.getRestaurant().getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .available(menu.getAvailable())
                .build();
    }
}
