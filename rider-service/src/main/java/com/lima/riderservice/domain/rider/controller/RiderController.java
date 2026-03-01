package com.lima.riderservice.domain.rider.controller;

import com.lima.riderservice.domain.rider.model.dto.RiderCreateRequest;
import com.lima.riderservice.domain.rider.model.dto.RiderLocationUpdate;
import com.lima.riderservice.domain.rider.model.dto.RiderResponse;
import com.lima.riderservice.domain.rider.model.type.RiderStatus;
import com.lima.riderservice.domain.rider.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;

    @PostMapping
    public ResponseEntity<RiderResponse> createRider(@Valid @RequestBody RiderCreateRequest request) {
        return ResponseEntity.ok(riderService.createRider(request));
    }

    @GetMapping("/{riderId}")
    public ResponseEntity<RiderResponse> getRider(@PathVariable Long riderId) {
        return ResponseEntity.ok(riderService.getRider(riderId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<RiderResponse>> getAvailableRiders(@RequestParam String region) {
        return ResponseEntity.ok(riderService.getAvailableRiders(region));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countAvailableRiders(@RequestParam String region) {
        return ResponseEntity.ok(riderService.countAvailableRiders(region));
    }

    @PatchMapping("/{riderId}/location")
    public ResponseEntity<RiderResponse> updateLocation(
            @PathVariable Long riderId,
            @Valid @RequestBody RiderLocationUpdate request) {
        return ResponseEntity.ok(riderService.updateLocation(riderId, request));
    }

    @PatchMapping("/{riderId}/status")
    public ResponseEntity<RiderResponse> changeStatus(
            @PathVariable Long riderId,
            @RequestParam RiderStatus status) {
        return ResponseEntity.ok(riderService.changeStatus(riderId, status));
    }

    @PostMapping("/{riderId}/assign/{orderId}")
    public ResponseEntity<RiderResponse> assignToOrder(
            @PathVariable Long riderId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(riderService.assignToOrder(riderId, orderId));
    }

    @PostMapping("/{riderId}/pickup/{orderId}")
    public ResponseEntity<RiderResponse> pickupOrder(
            @PathVariable Long riderId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(riderService.pickupOrder(riderId, orderId));
    }

    @PostMapping("/{riderId}/start-delivery/{orderId}")
    public ResponseEntity<RiderResponse> startDelivery(
            @PathVariable Long riderId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(riderService.startDelivery(riderId, orderId));
    }

    @PostMapping("/{riderId}/complete/{orderId}")
    public ResponseEntity<RiderResponse> completeDelivery(
            @PathVariable Long riderId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(riderService.completeDelivery(riderId, orderId));
    }
}
