package com.lima.restaurantservice.domain.restaurant.service;

import com.lima.restaurantservice.config.kafka.KafkaProducerService;
import com.lima.restaurantservice.domain.restaurant.model.dto.MenuCreateRequest;
import com.lima.restaurantservice.domain.restaurant.model.dto.MenuResponse;
import com.lima.restaurantservice.domain.restaurant.model.dto.RestaurantCreateRequest;
import com.lima.restaurantservice.domain.restaurant.model.dto.RestaurantEventDto;
import com.lima.restaurantservice.domain.restaurant.model.dto.RestaurantResponse;
import com.lima.restaurantservice.domain.restaurant.model.entity.Menu;
import com.lima.restaurantservice.domain.restaurant.model.entity.Restaurant;
import com.lima.restaurantservice.domain.restaurant.model.type.RestaurantStatus;
import com.lima.restaurantservice.domain.restaurant.repository.MenuRepository;
import com.lima.restaurantservice.domain.restaurant.repository.RestaurantRepository;
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
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .region(request.getRegion())
                .phone(request.getPhone())
                .category(request.getCategory())
                .status(RestaurantStatus.CLOSED)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("가맹점 생성 완료: restaurantId={}, name={}, region={}", saved.getId(), saved.getName(), saved.getRegion());

        return RestaurantResponse.from(saved);
    }

    public RestaurantResponse getRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + restaurantId));
        return RestaurantResponse.from(restaurant);
    }

    public List<RestaurantResponse> getRestaurantsByRegion(String region) {
        return restaurantRepository.findByRegion(region).stream()
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsByStatus(RestaurantStatus status) {
        return restaurantRepository.findByStatus(status).stream()
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResponse updateStatus(Long restaurantId, RestaurantStatus newStatus) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + restaurantId));

        restaurant.changeStatus(newStatus);
        Restaurant saved = restaurantRepository.save(restaurant);

        log.info("가맹점 상태 변경: restaurantId={}, newStatus={}", restaurantId, newStatus);
        return RestaurantResponse.from(saved);
    }

    @Transactional
    public void acceptOrder(Long restaurantId, Long orderId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + restaurantId));

        log.info("주문 수락: restaurantId={}, orderId={}", restaurantId, orderId);

        RestaurantEventDto event = RestaurantEventDto.orderAccepted(
                restaurantId, orderId, restaurant.getRegion(), restaurant.getAvgPrepTime());
        kafkaProducerService.sendRestaurantEvent(event);
    }

    @Transactional
    public void rejectOrder(Long restaurantId, Long orderId, String reason) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + restaurantId));

        log.info("주문 거부: restaurantId={}, orderId={}, reason={}", restaurantId, orderId, reason);

        RestaurantEventDto event = RestaurantEventDto.orderRejected(
                restaurantId, orderId, restaurant.getRegion());
        kafkaProducerService.sendRestaurantEvent(event);
    }

    @Transactional
    public void startPreparing(Long restaurantId, Long orderId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + restaurantId));

        log.info("조리 시작: restaurantId={}, orderId={}", restaurantId, orderId);

        RestaurantEventDto event = RestaurantEventDto.orderPreparing(
                restaurantId, orderId, restaurant.getRegion());
        kafkaProducerService.sendRestaurantEvent(event);
    }

    @Transactional
    public void orderReady(Long restaurantId, Long orderId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + restaurantId));

        log.info("조리 완료: restaurantId={}, orderId={}", restaurantId, orderId);

        RestaurantEventDto event = RestaurantEventDto.orderReady(
                restaurantId, orderId, restaurant.getRegion());
        kafkaProducerService.sendRestaurantEvent(event);
    }

    @Transactional
    public MenuResponse addMenu(Long restaurantId, MenuCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + restaurantId));

        Menu menu = Menu.builder()
                .name(request.getName())
                .price(request.getPrice())
                .available(true)
                .build();

        restaurant.addMenu(menu);
        restaurantRepository.save(restaurant);

        log.info("메뉴 추가: restaurantId={}, menuName={}, price={}", restaurantId, request.getName(), request.getPrice());
        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse updateMenuAvailability(Long restaurantId, Long menuId, Boolean available) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId));

        if (!menu.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("해당 가맹점의 메뉴가 아닙니다: restaurantId=" + restaurantId + ", menuId=" + menuId);
        }

        menu.setAvailable(available);
        Menu saved = menuRepository.save(menu);

        log.info("메뉴 판매 상태 변경: restaurantId={}, menuId={}, available={}", restaurantId, menuId, available);
        return MenuResponse.from(saved);
    }

    public List<MenuResponse> getMenusByRestaurant(Long restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId).stream()
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }
}
