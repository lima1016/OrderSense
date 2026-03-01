package com.lima.actionexecutorservice.config.kafka;

import com.lima.actionexecutorservice.domain.action.model.dto.ActionEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendActionEvent(ActionEventDto event) {
        String key = event.getActionLogId().toString();

        kafkaTemplate.send(KafkaConfig.ACTIONS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("액션 이벤트 발행 성공: actionLogId={}, anomalyType={}",
                                event.getActionLogId(), event.getAnomalyType());
                    } else {
                        log.error("액션 이벤트 발행 실패: actionLogId={}", event.getActionLogId(), ex);
                    }
                });
    }
}
