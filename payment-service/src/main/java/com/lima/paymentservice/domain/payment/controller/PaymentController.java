package com.lima.paymentservice.domain.payment.controller;

import com.lima.paymentservice.domain.payment.model.dto.PaymentCreateRequest;
import com.lima.paymentservice.domain.payment.model.dto.PaymentResponse;
import com.lima.paymentservice.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping
  public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentCreateRequest request) {
    return ResponseEntity.ok(paymentService.processPayment(request));
  }

  @GetMapping("/{paymentId}")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
    return ResponseEntity.ok(paymentService.getPayment(paymentId));
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
    return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
  }

  @PostMapping("/{paymentId}/refund")
  public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
    return ResponseEntity.ok(paymentService.refundPayment(paymentId));
  }
}
