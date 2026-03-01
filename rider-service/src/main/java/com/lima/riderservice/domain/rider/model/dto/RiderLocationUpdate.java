package com.lima.riderservice.domain.rider.model.dto;

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
public class RiderLocationUpdate {

    @NotNull(message = "라이더 ID는 필수입니다")
    private Long riderId;

    @NotNull(message = "위도는 필수입니다")
    private BigDecimal lat;

    @NotNull(message = "경도는 필수입니다")
    private BigDecimal lng;

    private String region;
}
