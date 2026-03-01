package com.lima.restaurantservice.config.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "orders", groupId = "restaurant-service-group")
    public void handleOrderEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Long orderId = ((Number) event.get("orderId")).longValue();
            Long restaurantId = ((Number) event.get("restaurantId")).longValue();

            log.info("주문 이벤트 수신: eventType={}, orderId={}, restaurantId={}", eventType, orderId, restaurantId);

            switch (eventType) {
                case "ORDER_CREATED" -> {
                    String region = (String) event.get("region");
                    log.info("신규 주문 접수 알림: orderId={}, restaurantId={}, region={}", orderId, restaurantId, region);
                    // 가맹점에 신규 주문 알림 로직 (향후 WebSocket 등으로 확장 가능)
                }
                case "ORDER_CANCELLED" -> {
                    log.info("주문 취소 알림: orderId={}, restaurantId={}", orderId, restaurantId);
                    // 가맹점에 주문 취소 알림 로직
                }
                default -> log.warn("알 수 없는 주문 이벤트 타입: {}", eventType);
            }
        } catch (Exception e) {
            log.error("주문 이벤트 처리 실패: {}", event, e);
        }
    }
}
