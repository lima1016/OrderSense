package com.lima.restaurantservice.config.kafka;

import com.lima.restaurantservice.domain.restaurant.model.dto.RestaurantEventDto;
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
     * 가맹점 이벤트를 Kafka로 발행
     */
    public void sendRestaurantEvent(RestaurantEventDto event) {
        String key = event.getRestaurantId().toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.RESTAURANTS_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Restaurant event sent successfully: eventType={}, restaurantId={}, orderId={}, partition={}, offset={}",
                        event.getEventType(),
                        event.getRestaurantId(),
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send restaurant event: eventType={}, restaurantId={}, orderId={}",
                        event.getEventType(),
                        event.getRestaurantId(),
                        event.getOrderId(),
                        ex);
            }
        });
    }
}
