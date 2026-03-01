from pydantic import BaseModel
from typing import Optional
from datetime import datetime


# ===== Kafka 수신 이벤트 =====

class OrderEvent(BaseModel):
    eventType: str  # ORDER_CREATED, ORDER_CANCELLED, ORDER_STATUS_CHANGED
    orderId: int
    customerId: Optional[int] = None
    restaurantId: Optional[int] = None
    riderId: Optional[int] = None
    region: Optional[str] = None
    status: Optional[str] = None
    previousStatus: Optional[str] = None
    totalAmount: Optional[float] = None
    itemCount: Optional[int] = None
    cancelReason: Optional[str] = None
    timestamp: Optional[str] = None


class RiderEvent(BaseModel):
    eventType: str  # RIDER_ASSIGNED, RIDER_PICKED_UP, DELIVERY_STARTED, DELIVERY_COMPLETED, LOCATION_UPDATED
    riderId: int
    orderId: Optional[int] = None
    region: Optional[str] = None
    lat: Optional[float] = None
    lng: Optional[float] = None
    status: Optional[str] = None
    timestamp: Optional[str] = None


class RestaurantEvent(BaseModel):
    eventType: str  # ORDER_ACCEPTED, ORDER_REJECTED, ORDER_PREPARING, ORDER_READY
    restaurantId: int
    orderId: Optional[int] = None
    region: Optional[str] = None
    prepTimeMinutes: Optional[int] = None
    timestamp: Optional[str] = None


class PaymentEvent(BaseModel):
    eventType: str  # PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_REFUNDED
    paymentId: Optional[int] = None
    orderId: int
    amount: Optional[float] = None
    method: Optional[str] = None
    status: Optional[str] = None
    transactionId: Optional[str] = None
    failureReason: Optional[str] = None
    timestamp: Optional[str] = None


# ===== Kafka 발행 이벤트 =====

class AnomalyEvent(BaseModel):
    anomalyType: str  # ORDER_DROP, DELIVERY_DELAY, CUSTOMER_CHURN, REJECTION_SPIKE
    region: Optional[str] = None
    description: str
    severity: float  # 0.0 ~ 1.0
    metadata: Optional[dict] = None
    detectedAt: str

    def to_kafka_dict(self) -> dict:
        return self.model_dump()


# ===== API 응답 모델 =====

class RegionStats(BaseModel):
    region: str
    order_count_1h: int = 0
    cancel_count_1h: int = 0
    cancel_rate: float = 0.0
    avg_delivery_minutes: Optional[float] = None
    available_riders: int = 0
    rejection_count_1h: int = 0
    payment_failure_count_1h: int = 0


class DemandForecast(BaseModel):
    region: str
    current_hour_orders: int
    predicted_next_hour_orders: float
    trend: str  # increasing, decreasing, stable
    confidence: float


class ChurnRiskCustomer(BaseModel):
    customer_id: int
    total_orders: int
    recent_orders: int
    order_frequency_drop: float
    risk_score: float
