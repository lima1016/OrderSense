package com.lima.riderservice.config.kafka;

import com.lima.riderservice.domain.rider.model.dto.RiderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 배달 이벤트를 Kafka로 발행
     */
    public void sendDeliveryEvent(RiderEventDto event) {
        String key = event.getRiderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.DELIVERIES_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("배달 이벤트 발행 성공: eventType={}, riderId={}, partition={}, offset={}",
                        event.getEventType(),
                        event.getRiderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("배달 이벤트 발행 실패: eventType={}, riderId={}",
                        event.getEventType(),
                        event.getRiderId(),
                        ex);
            }
        });
    }

    /**
     * 라이더 이벤트를 Kafka로 발행
     */
    public void sendRiderEvent(RiderEventDto event) {
        String key = event.getRiderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.RIDERS_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("라이더 이벤트 발행 성공: eventType={}, riderId={}, partition={}, offset={}",
                        event.getEventType(),
                        event.getRiderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("라이더 이벤트 발행 실패: eventType={}, riderId={}",
                        event.getEventType(),
                        event.getRiderId(),
                        ex);
            }
        });
    }
}
