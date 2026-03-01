package com.lima.orderservice.domain.order.model.entity;

import com.lima.orderservice.domain.order.model.type.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(nullable = false)
  private Long restaurantId;

  private Long riderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private OrderStatus status;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String deliveryAddress;

  @Column(precision = 10, scale = 8)
  private BigDecimal deliveryLat;

  @Column(precision = 11, scale = 8)
  private BigDecimal deliveryLng;

  @Column(nullable = false, length = 50)
  private String region;

  @Column(nullable = false)
  private LocalDateTime orderTime;

  private LocalDateTime acceptedTime;
  private LocalDateTime prepStartTime;
  private LocalDateTime readyTime;
  private LocalDateTime pickupTime;
  private LocalDateTime deliveredTime;
  private LocalDateTime cancelledTime;

  @Column(columnDefinition = "TEXT")
  private String cancelReason;

  private Integer estimatedDeliveryMinutes;
  private Integer actualDeliveryMinutes;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  // 비즈니스 메서드
  public void changeStatus(OrderStatus newStatus) {
    this.status = newStatus;

    switch (newStatus) {
      case ACCEPTED -> this.acceptedTime = LocalDateTime.now();
      case PREPARING -> this.prepStartTime = LocalDateTime.now();
      case READY -> this.readyTime = LocalDateTime.now();
      case PICKED_UP -> this.pickupTime = LocalDateTime.now();
      case DELIVERED -> {
        this.deliveredTime = LocalDateTime.now();
        calculateActualDeliveryTime();
      }
      case CANCELLED -> this.cancelledTime = LocalDateTime.now();
    }
  }

  public void cancel(String reason) {
    this.status = OrderStatus.CANCELLED;
    this.cancelReason = reason;
    this.cancelledTime = LocalDateTime.now();
  }

  public void assignRider(Long riderId) {
    this.riderId = riderId;
  }

  private void calculateActualDeliveryTime() {
    if (orderTime != null && deliveredTime != null) {
      this.actualDeliveryMinutes = (int) java.time.Duration
          .between(orderTime, deliveredTime)
          .toMinutes();
    }
  }

  public void addOrderItem(OrderItem item) {
    items.add(item);
    item.setOrder(this);
  }

  public void updateTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }
}