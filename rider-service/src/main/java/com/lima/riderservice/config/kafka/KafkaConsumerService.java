package com.lima.riderservice.config.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "orders", groupId = "rider-service-group")
    public void handleOrderEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Long orderId = ((Number) event.get("orderId")).longValue();

            log.info("주문 이벤트 수신: eventType={}, orderId={}", eventType, orderId);

            switch (eventType) {
                case "ORDER_CREATED" -> {
                    String region = (String) event.get("region");
                    log.info("새 주문 접수 - 배달 가능 라이더 확인 필요: orderId={}, region={}", orderId, region);
                }
                default -> log.warn("알 수 없는 주문 이벤트 타입: {}", eventType);
            }
        } catch (Exception e) {
            log.error("주문 이벤트 처리 실패: {}", event, e);
        }
    }
}
