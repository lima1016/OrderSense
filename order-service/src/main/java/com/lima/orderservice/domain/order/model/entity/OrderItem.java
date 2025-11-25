package com.lima.orderservice.domain.order.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @Setter
  private Order order;

  @Column(nullable = false, length = 200)
  private String menuItemName;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal totalPrice;

  public static OrderItem create(String menuItemName, Integer quantity, BigDecimal unitPrice) {
    return OrderItem.builder()
        .menuItemName(menuItemName)
        .quantity(quantity)
        .unitPrice(unitPrice)
        .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
        .build();
  }
}