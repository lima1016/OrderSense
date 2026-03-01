package com.lima.riderservice.domain.rider.service;

import com.lima.riderservice.config.kafka.KafkaProducerService;
import com.lima.riderservice.domain.rider.model.dto.RiderCreateRequest;
import com.lima.riderservice.domain.rider.model.dto.RiderEventDto;
import com.lima.riderservice.domain.rider.model.dto.RiderLocationUpdate;
import com.lima.riderservice.domain.rider.model.dto.RiderResponse;
import com.lima.riderservice.domain.rider.model.entity.Rider;
import com.lima.riderservice.domain.rider.model.type.RiderStatus;
import com.lima.riderservice.domain.rider.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RiderService {

    private final RiderRepository riderRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public RiderResponse createRider(RiderCreateRequest request) {
        Rider rider = Rider.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .currentRegion(request.getCurrentRegion())
                .status(RiderStatus.OFFLINE)
                .build();

        Rider savedRider = riderRepository.save(rider);
        log.info("라이더 등록 완료: riderId={}, name={}", savedRider.getId(), savedRider.getName());

        return RiderResponse.from(savedRider);
    }

    public RiderResponse getRider(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더를 찾을 수 없습니다: " + riderId));
        return RiderResponse.from(rider);
    }

    public List<RiderResponse> getAvailableRiders(String region) {
        return riderRepository.findByCurrentRegionAndStatus(region, RiderStatus.AVAILABLE).stream()
                .map(RiderResponse::from)
                .collect(Collectors.toList());
    }

    public Long countAvailableRiders(String region) {
        return riderRepository.countAvailableRidersByRegion(region);
    }

    @Transactional
    public RiderResponse updateLocation(Long riderId, RiderLocationUpdate request) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더를 찾을 수 없습니다: " + riderId));

        rider.updateLocation(request.getLat(), request.getLng(), request.getRegion());
        Rider savedRider = riderRepository.save(rider);

        log.info("라이더 위치 업데이트: riderId={}, lat={}, lng={}, region={}",
                riderId, request.getLat(), request.getLng(), request.getRegion());

        kafkaProducerService.sendRiderEvent(
                RiderEventDto.createLocationUpdated(riderId, request.getLat(), request.getLng(), request.getRegion())
        );

        return RiderResponse.from(savedRider);
    }

    @Transactional
    public RiderResponse assignToOrder(Long riderId, Long orderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더를 찾을 수 없습니다: " + riderId));

        rider.changeStatus(RiderStatus.BUSY);
        Rider savedRider = riderRepository.save(rider);

        log.info("라이더 주문 배정: riderId={}, orderId={}", riderId, orderId);

        kafkaProducerService.sendDeliveryEvent(
                RiderEventDto.createRiderAssigned(riderId, orderId, rider.getCurrentRegion())
        );

        return RiderResponse.from(savedRider);
    }

    @Transactional
    public RiderResponse pickupOrder(Long riderId, Long orderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더를 찾을 수 없습니다: " + riderId));

        log.info("라이더 픽업 완료: riderId={}, orderId={}", riderId, orderId);

        kafkaProducerService.sendDeliveryEvent(
                RiderEventDto.createPickedUp(riderId, orderId)
        );

        return RiderResponse.from(rider);
    }

    @Transactional
    public RiderResponse startDelivery(Long riderId, Long orderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더를 찾을 수 없습니다: " + riderId));

        log.info("배달 시작: riderId={}, orderId={}", riderId, orderId);

        kafkaProducerService.sendDeliveryEvent(
                RiderEventDto.createDeliveryStarted(riderId, orderId)
        );

        return RiderResponse.from(rider);
    }

    @Transactional
    public RiderResponse completeDelivery(Long riderId, Long orderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더를 찾을 수 없습니다: " + riderId));

        rider.changeStatus(RiderStatus.AVAILABLE);
        Rider savedRider = riderRepository.save(rider);

        log.info("배달 완료: riderId={}, orderId={}", riderId, orderId);

        kafkaProducerService.sendDeliveryEvent(
                RiderEventDto.createDeliveryCompleted(riderId, orderId)
        );

        return RiderResponse.from(savedRider);
    }

    @Transactional
    public RiderResponse changeStatus(Long riderId, RiderStatus status) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더를 찾을 수 없습니다: " + riderId));

        rider.changeStatus(status);
        Rider savedRider = riderRepository.save(rider);

        log.info("라이더 상태 변경: riderId={}, status={}", riderId, status);

        return RiderResponse.from(savedRider);
    }
}
