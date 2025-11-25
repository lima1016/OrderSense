package com.lima.orderservice.domain.order.model.type;

public enum OrderStatus {
  PENDING,        // 주문 생성됨
  ACCEPTED,       // 가맹점이 수락
  PREPARING,      // 음식 준비 중
  READY,          // 픽업 준비 완료
  PICKED_UP,      // 라이더가 픽업
  DELIVERING,     // 배달 중
  DELIVERED,      // 배달 완료
  CANCELLED       // 주문 취소
}