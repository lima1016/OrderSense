package com.lima.paymentservice.domain.payment.repository;

import com.lima.paymentservice.domain.payment.model.entity.Payment;
import com.lima.paymentservice.domain.payment.model.type.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  // 주문 ID로 결제 조회
  Optional<Payment> findByOrderId(Long orderId);

  // 상태별 결제 조회
  List<Payment> findByStatus(PaymentStatus status);

  // 특정 기간 내 실패한 결제 건수
  @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt BETWEEN :startTime AND :endTime")
  Long countFailedPayments(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime
  );
}
