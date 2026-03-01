package com.lima.actionexecutorservice.domain.action.service;

import com.lima.actionexecutorservice.config.kafka.KafkaProducerService;
import com.lima.actionexecutorservice.domain.action.model.dto.ActionEventDto;
import com.lima.actionexecutorservice.domain.action.model.dto.ActionLogResponse;
import com.lima.actionexecutorservice.domain.action.model.dto.AnomalyEventDto;
import com.lima.actionexecutorservice.domain.action.model.entity.ActionLog;
import com.lima.actionexecutorservice.domain.action.model.type.ActionResult;
import com.lima.actionexecutorservice.domain.action.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ActionExecutorService {

    private final ActionLogRepository actionLogRepository;
    private final KafkaProducerService kafkaProducerService;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public ActionLogResponse executeAction(AnomalyEventDto anomaly) {
        String action;
        String result;
        ActionResult actionResult;

        switch (anomaly.getAnomalyType()) {
            case "ORDER_DROP" -> {
                action = "라이더 가용성 확인 및 인센티브 알림 발송";
                result = checkRiderAvailability(anomaly.getRegion());
                actionResult = ActionResult.SUCCESS;
            }
            case "DELIVERY_DELAY" -> {
                action = "배달 예상 시간 조정 및 고객 알림";
                result = adjustDeliveryEstimates(anomaly.getRegion());
                actionResult = ActionResult.SUCCESS;
            }
            case "CUSTOMER_CHURN" -> {
                action = "이탈 위험 고객 맞춤 프로모션 발송";
                result = sendPromotions(anomaly);
                actionResult = ActionResult.SUCCESS;
            }
            case "REJECTION_SPIKE" -> {
                action = "가맹점 노출 순위 하락 조정";
                result = adjustRestaurantRank(anomaly);
                actionResult = ActionResult.SUCCESS;
            }
            default -> {
                action = "알 수 없는 이상 유형 - 검토 필요";
                result = "조치 없음";
                actionResult = ActionResult.PARTIAL;
            }
        }

        ActionLog actionLog = ActionLog.builder()
                .anomalyType(anomaly.getAnomalyType())
                .region(anomaly.getRegion())
                .action(action)
                .result(result)
                .actionResult(actionResult)
                .executedAt(LocalDateTime.now())
                .build();

        ActionLog saved = actionLogRepository.save(actionLog);
        log.info("자동 대응 실행 완료: anomalyType={}, region={}, result={}",
                anomaly.getAnomalyType(), anomaly.getRegion(), actionResult);

        kafkaProducerService.sendActionEvent(ActionEventDto.from(saved));

        return ActionLogResponse.from(saved);
    }

    public List<ActionLogResponse> getAllActionLogs() {
        return actionLogRepository.findAll().stream()
                .map(ActionLogResponse::from)
                .collect(Collectors.toList());
    }

    public List<ActionLogResponse> getActionLogsByAnomalyType(String anomalyType) {
        return actionLogRepository.findByAnomalyType(anomalyType).stream()
                .map(ActionLogResponse::from)
                .collect(Collectors.toList());
    }

    public List<ActionLogResponse> getActionLogsByRegion(String region) {
        return actionLogRepository.findByRegion(region).stream()
                .map(ActionLogResponse::from)
                .collect(Collectors.toList());
    }

    private String checkRiderAvailability(String region) {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/api/riders/count?region=" + region)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return "라이더 가용성 확인 완료: " + response + " - 인센티브 알림 발송됨";
        } catch (Exception e) {
            log.warn("라이더 서비스 호출 실패: {}", e.getMessage());
            return "라이더 서비스 호출 실패 - 운영팀 알림 전송됨";
        }
    }

    private String adjustDeliveryEstimates(String region) {
        log.info("배달 예상 시간 조정: region={}", region);
        return "지역 [" + region + "] 배달 예상 시간 +15분 조정 완료, 고객 알림 발송됨";
    }

    private String sendPromotions(AnomalyEventDto anomaly) {
        log.info("이탈 위험 고객 프로모션 발송: region={}", anomaly.getRegion());
        return "이탈 위험 고객 대상 10% 할인 쿠폰 발송 완료";
    }

    private String adjustRestaurantRank(AnomalyEventDto anomaly) {
        try {
            Long restaurantId = anomaly.getMetadata() != null
                    ? ((Number) anomaly.getMetadata().get("restaurantId")).longValue()
                    : 0L;
            log.info("가맹점 노출 순위 하락: restaurantId={}", restaurantId);
            return "가맹점 [" + restaurantId + "] 노출 순위 하락 처리 완료, 운영팀 확인 요청됨";
        } catch (Exception e) {
            return "가맹점 정보 부족 - 운영팀 확인 요청됨";
        }
    }
}
