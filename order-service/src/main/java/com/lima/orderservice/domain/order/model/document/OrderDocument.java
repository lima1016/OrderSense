package com.lima.orderservice.domain.order.model.document;

import com.lima.orderservice.domain.order.model.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Document(indexName = "ordersense-order-search")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDocument {

    @Id
    private Long id;

    private Long orderId;

    private Long customerId;

    @Field(type = FieldType.Text)
    private String customerName;

    private Long restaurantId;
    private Long riderId;

    @Field(type = FieldType.Keyword)
    private String status;

    private BigDecimal totalAmount;

    @Field(type = FieldType.Text)
    private String deliveryAddress;

    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;

    @Field(type = FieldType.Keyword)
    private String region;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime orderTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime acceptedTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime prepStartTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime readyTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime pickupTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime deliveredTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime cancelledTime;

    @Field(type = FieldType.Text)
    private String cancelReason;

    private Integer estimatedDeliveryMinutes;
    private Integer actualDeliveryMinutes;

    @Field(type = FieldType.Nested)
    private List<OrderItemDocument> items;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime createdAt;

    public static OrderDocument from(Order order) {
        return OrderDocument.builder()
                .id(order.getId())
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .restaurantId(order.getRestaurantId())
                .riderId(order.getRiderId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryLat(order.getDeliveryLat())
                .deliveryLng(order.getDeliveryLng())
                .region(order.getRegion())
                .orderTime(order.getOrderTime())
                .acceptedTime(order.getAcceptedTime())
                .prepStartTime(order.getPrepStartTime())
                .readyTime(order.getReadyTime())
                .pickupTime(order.getPickupTime())
                .deliveredTime(order.getDeliveredTime())
                .cancelledTime(order.getCancelledTime())
                .cancelReason(order.getCancelReason())
                .estimatedDeliveryMinutes(order.getEstimatedDeliveryMinutes())
                .actualDeliveryMinutes(order.getActualDeliveryMinutes())
                .items(order.getItems().stream()
                        .map(OrderItemDocument::from)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
