package com.lima.actionexecutorservice.domain.action.model.dto;

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
public class AnomalyEventDto {

    private String anomalyType;    // ORDER_DROP, DELIVERY_DELAY, CUSTOMER_CHURN, REJECTION_SPIKE
    private String region;
    private String description;
    private Double severity;       // 0.0 ~ 1.0
    private Map<String, Object> metadata;
    private LocalDateTime detectedAt;
}
