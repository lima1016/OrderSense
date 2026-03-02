package com.lima.orderservice.domain.order.service;

import com.lima.orderservice.config.kafka.KafkaProducerService;
import com.lima.orderservice.domain.order.model.dto.OrderCreateRequest;
import com.lima.orderservice.domain.order.model.dto.OrderEventDto;
import com.lima.orderservice.domain.order.model.dto.OrderResponse;
import com.lima.orderservice.domain.order.model.dto.OrderStatsResponse;
import com.lima.orderservice.domain.order.model.entity.Customer;
import com.lima.orderservice.domain.order.model.entity.Order;
import com.lima.orderservice.domain.order.model.entity.OrderItem;
import com.lima.orderservice.domain.order.model.type.OrderStatus;
import com.lima.orderservice.domain.order.repository.CustomerRepository;
import com.lima.orderservice.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final KafkaProducerService kafkaProducerService;
    private final OrderSearchService orderSearchService;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        Order order = Order.builder()
                .customer(customer)
                .restaurantId(request.getRestaurantId())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryLat(request.getDeliveryLat())
                .deliveryLng(request.getDeliveryLng())
                .region(request.getRegion())
                .orderTime(LocalDateTime.now())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateRequest.OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = OrderItem.create(itemReq.getMenuItemName(), itemReq.getQuantity(), itemReq.getUnitPrice());
            order.addOrderItem(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        order.updateTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료: orderId={}, region={}, totalAmount={}", savedOrder.getId(), savedOrder.getRegion(), totalAmount);

        kafkaProducerService.sendOrderEvent(OrderEventDto.createOrderCreated(savedOrder));
        orderSearchService.indexOrder(savedOrder);

        return OrderResponse.from(savedOrder);
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByRegion(String region) {
        return orderRepository.findByRegion(region).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        OrderStatus previousStatus = order.getStatus();
        order.changeStatus(newStatus);
        Order saved = orderRepository.save(order);

        log.info("주문 상태 변경: orderId={}, {} -> {}", orderId, previousStatus, newStatus);
        kafkaProducerService.sendOrderEvent(OrderEventDto.createOrderStatusChanged(saved, previousStatus));
        orderSearchService.indexOrder(saved);

        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        order.cancel(reason);
        Order saved = orderRepository.save(order);

        log.info("주문 취소: orderId={}, reason={}", orderId, reason);
        kafkaProducerService.sendOrderEvent(OrderEventDto.createOrderCancelled(saved));
        orderSearchService.indexOrder(saved);

        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse assignRider(Long orderId, Long riderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        order.assignRider(riderId);
        Order saved = orderRepository.save(order);

        log.info("라이더 배정: orderId={}, riderId={}", orderId, riderId);
        orderSearchService.indexOrder(saved);
        return OrderResponse.from(saved);
    }

    @Cacheable(value = "orderStats", key = "#region + '_' + #startTime + '_' + #endTime")
    public OrderStatsResponse getOrderStats(String region, LocalDateTime startTime, LocalDateTime endTime) {
        Long totalOrders = orderRepository.countByRegionAndTimePeriod(region, startTime, endTime);
        Long cancelledOrders = orderRepository.countCancelledOrders(startTime, endTime);
        Double avgDeliveryMinutes = orderRepository.getAverageDeliveryTimeByRegion(region, startTime, endTime);

        double cancellationRate = totalOrders > 0 ? (cancelledOrders * 100.0 / totalOrders) : 0.0;

        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.findByRegionAndStatus(region, status).stream()
                    .filter(o -> !o.getOrderTime().isBefore(startTime) && !o.getOrderTime().isAfter(endTime))
                    .count();
            ordersByStatus.put(status.name(), count);
        }

        return OrderStatsResponse.builder()
                .region(region)
                .startTime(startTime)
                .endTime(endTime)
                .totalOrders(totalOrders)
                .cancelledOrders(cancelledOrders)
                .cancellationRate(cancellationRate)
                .averageDeliveryMinutes(avgDeliveryMinutes)
                .ordersByStatus(ordersByStatus)
                .build();
    }
}
