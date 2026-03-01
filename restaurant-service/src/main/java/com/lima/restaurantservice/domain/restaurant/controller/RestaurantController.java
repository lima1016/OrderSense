package com.lima.restaurantservice.domain.restaurant.controller;

import com.lima.restaurantservice.domain.restaurant.model.dto.MenuCreateRequest;
import com.lima.restaurantservice.domain.restaurant.model.dto.MenuResponse;
import com.lima.restaurantservice.domain.restaurant.model.dto.RestaurantCreateRequest;
import com.lima.restaurantservice.domain.restaurant.model.dto.RestaurantResponse;
import com.lima.restaurantservice.domain.restaurant.model.type.RestaurantStatus;
import com.lima.restaurantservice.domain.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantCreateRequest request) {
        return ResponseEntity.ok(restaurantService.createRestaurant(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurant(id));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<RestaurantResponse>> getByRegion(@PathVariable String region) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByRegion(region));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RestaurantResponse>> getByStatus(@PathVariable RestaurantStatus status) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RestaurantResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam RestaurantStatus status) {
        return ResponseEntity.ok(restaurantService.updateStatus(id, status));
    }

    @PostMapping("/{id}/orders/{orderId}/accept")
    public ResponseEntity<Void> acceptOrder(@PathVariable Long id, @PathVariable Long orderId) {
        restaurantService.acceptOrder(id, orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/orders/{orderId}/reject")
    public ResponseEntity<Void> rejectOrder(
            @PathVariable Long id,
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "가맹점 사정으로 주문 거부") String reason) {
        restaurantService.rejectOrder(id, orderId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/orders/{orderId}/preparing")
    public ResponseEntity<Void> startPreparing(@PathVariable Long id, @PathVariable Long orderId) {
        restaurantService.startPreparing(id, orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/orders/{orderId}/ready")
    public ResponseEntity<Void> orderReady(@PathVariable Long id, @PathVariable Long orderId) {
        restaurantService.orderReady(id, orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/menus")
    public ResponseEntity<MenuResponse> addMenu(
            @PathVariable Long id,
            @Valid @RequestBody MenuCreateRequest request) {
        return ResponseEntity.ok(restaurantService.addMenu(id, request));
    }

    @PatchMapping("/{id}/menus/{menuId}/availability")
    public ResponseEntity<MenuResponse> updateMenuAvailability(
            @PathVariable Long id,
            @PathVariable Long menuId,
            @RequestParam Boolean available) {
        return ResponseEntity.ok(restaurantService.updateMenuAvailability(id, menuId, available));
    }

    @GetMapping("/{id}/menus")
    public ResponseEntity<List<MenuResponse>> getMenus(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenusByRestaurant(id));
    }
}
