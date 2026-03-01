package com.lima.riderservice.domain.rider.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderCreateRequest {

    @NotBlank(message = "라이더 이름은 필수입니다")
    private String name;

    private String phone;

    private String currentRegion;
}
