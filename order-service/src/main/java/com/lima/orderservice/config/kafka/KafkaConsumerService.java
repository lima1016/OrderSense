package com.lima.orderservice.config.kafka;

import com.lima.orderservice.domain.order.model.type.OrderStatus;
import com.lima.orderservice.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final OrderService orderService;

    @KafkaListener(topics = "deliveries", groupId = "order-service-group")
    public void handleDeliveryEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Long orderId = ((Number) event.get("orderId")).longValue();

            log.info("배달 이벤트 수신: eventType={}, orderId={}", eventType, orderId);

            switch (eventType) {
                case "RIDER_ASSIGNED" -> {
                    Long riderId = ((Number) event.get("riderId")).longValue();
                    orderService.assignRider(orderId, riderId);
                }
                case "RIDER_PICKED_UP" -> orderService.updateOrderStatus(orderId, OrderStatus.PICKED_UP);
                case "DELIVERY_STARTED" -> orderService.updateOrderStatus(orderId, OrderStatus.DELIVERING);
                case "DELIVERY_COMPLETED" -> orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
                default -> log.warn("알 수 없는 배달 이벤트 타입: {}", eventType);
            }
        } catch (Exception e) {
            log.error("배달 이벤트 처리 실패: {}", event, e);
        }
    }

    @KafkaListener(topics = "restaurants", groupId = "order-service-group")
    public void handleRestaurantEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Long orderId = ((Number) event.get("orderId")).longValue();

            log.info("가맹점 이벤트 수신: eventType={}, orderId={}", eventType, orderId);

            switch (eventType) {
                case "ORDER_ACCEPTED" -> orderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED);
                case "ORDER_PREPARING" -> orderService.updateOrderStatus(orderId, OrderStatus.PREPARING);
                case "ORDER_READY" -> orderService.updateOrderStatus(orderId, OrderStatus.READY);
                case "ORDER_REJECTED" -> orderService.cancelOrder(orderId, "가맹점 주문 거부");
                default -> log.warn("알 수 없는 가맹점 이벤트 타입: {}", eventType);
            }
        } catch (Exception e) {
            log.error("가맹점 이벤트 처리 실패: {}", event, e);
        }
    }

    @KafkaListener(topics = "payments", groupId = "order-service-group")
    public void handlePaymentEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Long orderId = ((Number) event.get("orderId")).longValue();

            log.info("결제 이벤트 수신: eventType={}, orderId={}", eventType, orderId);

            switch (eventType) {
                case "PAYMENT_COMPLETED" -> log.info("결제 완료 확인: orderId={}", orderId);
                case "PAYMENT_FAILED" -> orderService.cancelOrder(orderId, "결제 실패");
                default -> log.warn("알 수 없는 결제 이벤트 타입: {}", eventType);
            }
        } catch (Exception e) {
            log.error("결제 이벤트 처리 실패: {}", event, e);
        }
    }
}
