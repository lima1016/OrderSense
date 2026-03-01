package com.lima.riderservice.domain.rider.model.dto;

import com.lima.riderservice.domain.rider.model.type.RiderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderEventDto {

    private String eventType;
    private Long riderId;
    private Long orderId;
    private String region;
    private BigDecimal lat;
    private BigDecimal lng;
    private RiderStatus status;
    private LocalDateTime timestamp;

    public static RiderEventDto createRiderAssigned(Long riderId, Long orderId, String region) {
        return RiderEventDto.builder()
                .eventType("RIDER_ASSIGNED")
                .riderId(riderId)
                .orderId(orderId)
                .region(region)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RiderEventDto createPickedUp(Long riderId, Long orderId) {
        return RiderEventDto.builder()
                .eventType("RIDER_PICKED_UP")
                .riderId(riderId)
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RiderEventDto createDeliveryStarted(Long riderId, Long orderId) {
        return RiderEventDto.builder()
                .eventType("DELIVERY_STARTED")
                .riderId(riderId)
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RiderEventDto createDeliveryCompleted(Long riderId, Long orderId) {
        return RiderEventDto.builder()
                .eventType("DELIVERY_COMPLETED")
                .riderId(riderId)
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RiderEventDto createLocationUpdated(Long riderId, BigDecimal lat, BigDecimal lng, String region) {
        return RiderEventDto.builder()
                .eventType("LOCATION_UPDATED")
                .riderId(riderId)
                .lat(lat)
                .lng(lng)
                .region(region)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
