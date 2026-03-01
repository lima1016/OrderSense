package com.lima.restaurantservice.domain.restaurant.repository;

import com.lima.restaurantservice.domain.restaurant.model.entity.Restaurant;
import com.lima.restaurantservice.domain.restaurant.model.type.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // 지역별 가맹점 조회
    List<Restaurant> findByRegion(String region);

    // 상태별 가맹점 조회
    List<Restaurant> findByStatus(RestaurantStatus status);

    // 카테고리별 가맹점 조회
    List<Restaurant> findByCategory(String category);

    // 지역 + 상태별 가맹점 조회
    List<Restaurant> findByRegionAndStatus(String region, RestaurantStatus status);
}
