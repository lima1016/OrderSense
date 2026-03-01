package com.lima.riderservice.domain.rider.model.dto;

import com.lima.riderservice.domain.rider.model.entity.Rider;
import com.lima.riderservice.domain.rider.model.type.RiderStatus;
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
public class RiderResponse {

    private Long riderId;
    private String name;
    private String phone;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private RiderStatus status;
    private String currentRegion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RiderResponse from(Rider rider) {
        return RiderResponse.builder()
                .riderId(rider.getId())
                .name(rider.getName())
                .phone(rider.getPhone())
                .currentLat(rider.getCurrentLat())
                .currentLng(rider.getCurrentLng())
                .status(rider.getStatus())
                .currentRegion(rider.getCurrentRegion())
                .createdAt(rider.getCreatedAt())
                .updatedAt(rider.getUpdatedAt())
                .build();
    }
}
