package com.lima.orderservice.domain.order.controller;

import com.lima.orderservice.domain.order.model.dto.OrderCreateRequest;
import com.lima.orderservice.domain.order.model.dto.OrderResponse;
import com.lima.orderservice.domain.order.model.dto.OrderStatsResponse;
import com.lima.orderservice.domain.order.model.type.OrderStatus;
import com.lima.orderservice.domain.order.service.OrderSearchService;
import com.lima.orderservice.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderSearchService orderSearchService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<OrderResponse>> getOrdersByRegion(@PathVariable String region) {
        return ResponseEntity.ok(orderService.getOrdersByRegion(region));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, reason));
    }

    @PatchMapping("/{orderId}/rider")
    public ResponseEntity<OrderResponse> assignRider(
            @PathVariable Long orderId,
            @RequestParam Long riderId) {
        return ResponseEntity.ok(orderService.assignRider(orderId, riderId));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchOrders(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderSearchService.search(q, status, region, page, size));
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindexAll() {
        int count = orderSearchService.reindexAll();
        return ResponseEntity.ok(Map.of("message", "리인덱싱 완료", "count", count));
    }

    @GetMapping("/stats")
    public ResponseEntity<OrderStatsResponse> getStats(
            @RequestParam String region,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(orderService.getOrderStats(region, startTime, endTime));
    }
}
