package com.lima.restaurantservice.domain.restaurant.repository;

import com.lima.restaurantservice.domain.restaurant.model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 가맹점별 메뉴 조회
    List<Menu> findByRestaurantId(Long restaurantId);

    // 가맹점별 판매 가능 메뉴 조회
    List<Menu> findByRestaurantIdAndAvailableTrue(Long restaurantId);
}
