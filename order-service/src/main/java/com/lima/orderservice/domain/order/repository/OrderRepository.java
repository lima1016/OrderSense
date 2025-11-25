package com.lima.orderservice.domain.order.repository;

import com.lima.orderservice.domain.order.model.entity.Order;
import com.lima.orderservice.domain.order.model.type.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  // 고객별 주문 조회
  List<Order> findByCustomerId(Long customerId);

  // 가맹점별 주문 조회
  List<Order> findByRestaurantId(Long restaurantId);

  // 라이더별 주문 조회
  List<Order> findByRiderId(Long riderId);

  // 상태별 주문 조회
  List<Order> findByStatus(OrderStatus status);

  // 지역별 주문 조회
  List<Order> findByRegion(String region);

  // 지역 + 상태별 주문 조회
  List<Order> findByRegionAndStatus(String region, OrderStatus status);

  // 특정 기간 주문 조회
  List<Order> findByOrderTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

  // 지역 + 기간별 주문 수 (이상 탐지용)
  @Query("SELECT COUNT(o) FROM Order o WHERE o.region = :region AND o.orderTime BETWEEN :startTime AND :endTime")
  Long countByRegionAndTimePeriod(
      @Param("region") String region,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime
  );

  // 지역별 평균 배달 시간 (이상 탐지용)
  @Query("SELECT AVG(o.actualDeliveryMinutes) FROM Order o WHERE o.region = :region AND o.status = 'DELIVERED' AND o.orderTime BETWEEN :startTime AND :endTime")
  Double getAverageDeliveryTimeByRegion(
      @Param("region") String region,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime
  );

  // 가맹점별 주문 거부율 계산용
  @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = 'CANCELLED' AND o.cancelReason LIKE '%거부%' AND o.orderTime BETWEEN :startTime AND :endTime")
  Long countRejectedOrdersByRestaurant(
      @Param("restaurantId") Long restaurantId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime
  );

  // 고객별 최근 주문 조회 (이탈 예측용)
  @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.orderTime DESC")
  List<Order> findRecentOrdersByCustomer(@Param("customerId") Long customerId);

  // 주문 취소율 조회
  @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED' AND o.orderTime BETWEEN :startTime AND :endTime")
  Long countCancelledOrders(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime
  );
}