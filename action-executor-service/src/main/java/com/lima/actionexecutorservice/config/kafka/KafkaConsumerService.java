package com.lima.actionexecutorservice.config.kafka;

import com.lima.actionexecutorservice.domain.action.model.dto.AnomalyEventDto;
import com.lima.actionexecutorservice.domain.action.service.ActionExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ActionExecutorService actionExecutorService;

    @KafkaListener(topics = "anomalies", groupId = "action-executor-service-group")
    public void handleAnomalyEvent(Map<String, Object> event) {
        try {
            String anomalyType = (String) event.get("anomalyType");
            String region = (String) event.get("region");
            String description = (String) event.get("description");
            Double severity = event.get("severity") != null ? ((Number) event.get("severity")).doubleValue() : 0.0;

            log.info("이상 탐지 이벤트 수신: anomalyType={}, region={}, severity={}", anomalyType, region, severity);

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = event.get("metadata") != null
                    ? (Map<String, Object>) event.get("metadata")
                    : null;

            AnomalyEventDto anomalyEvent = AnomalyEventDto.builder()
                    .anomalyType(anomalyType)
                    .region(region)
                    .description(description)
                    .severity(severity)
                    .metadata(metadata)
                    .detectedAt(LocalDateTime.now())
                    .build();

            actionExecutorService.executeAction(anomalyEvent);
        } catch (Exception e) {
            log.error("이상 탐지 이벤트 처리 실패: {}", event, e);
        }
    }
}
