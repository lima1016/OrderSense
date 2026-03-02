import threading
from collections import defaultdict
from datetime import datetime, timedelta
from typing import Optional

import config


class EventStore:
    """인메모리 이벤트 저장소 - 시계열 집계 및 분석용"""

    def __init__(self):
        self._lock = threading.Lock()

        # 주문 이벤트
        self.order_events: list[dict] = []
        # 배달 이벤트
        self.delivery_events: list[dict] = []
        # 라이더 이벤트
        self.rider_events: list[dict] = []
        # 가맹점 이벤트
        self.restaurant_events: list[dict] = []
        # 결제 이벤트
        self.payment_events: list[dict] = []

        # 탐지된 이상 기록
        self.detected_anomalies: list[dict] = []

        # 라이더 상태 추적 (최신 상태)
        self.rider_status: dict[int, dict] = {}
        # 고객별 주문 이력
        self.customer_orders: dict[int, list[datetime]] = defaultdict(list)

    def add_order_event(self, event: dict):
        with self._lock:
            event["_received_at"] = datetime.now()
            self.order_events.append(event)

            customer_id = event.get("customerId")
            if customer_id and event.get("eventType") == "ORDER_CREATED":
                self.customer_orders[customer_id].append(datetime.now())

    def add_delivery_event(self, event: dict):
        with self._lock:
            event["_received_at"] = datetime.now()
            self.delivery_events.append(event)

    def add_rider_event(self, event: dict):
        with self._lock:
            event["_received_at"] = datetime.now()
            self.rider_events.append(event)

            rider_id = event.get("riderId")
            if rider_id:
                self.rider_status[rider_id] = {
                    "status": event.get("status"),
                    "region": event.get("region"),
                    "updatedAt": datetime.now(),
                }

    def add_restaurant_event(self, event: dict):
        with self._lock:
            event["_received_at"] = datetime.now()
            self.restaurant_events.append(event)

    def add_payment_event(self, event: dict):
        with self._lock:
            event["_received_at"] = datetime.now()
            self.payment_events.append(event)

    def add_anomaly(self, anomaly: dict):
        with self._lock:
            self.detected_anomalies.append(anomaly)

    def get_recent_anomalies(
        self,
        limit: int = 50,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None,
    ) -> list[dict]:
        with self._lock:
            anomalies = self.detected_anomalies

            if start_date or end_date:
                filtered = []
                for a in anomalies:
                    detected_at_str = a.get("detectedAt")
                    if not detected_at_str:
                        continue
                    try:
                        detected_at = datetime.fromisoformat(
                            detected_at_str.replace("Z", "+00:00")
                        ).replace(tzinfo=None)
                    except (ValueError, TypeError):
                        continue
                    if start_date and detected_at < start_date:
                        continue
                    if end_date and detected_at > end_date:
                        continue
                    filtered.append(a)
                anomalies = filtered

            return list(reversed(anomalies[-limit:]))

    def cleanup_old_events(self):
        """보관 기간이 지난 이벤트 정리"""
        cutoff = datetime.now() - timedelta(hours=config.EVENT_RETENTION_HOURS)
        with self._lock:
            self.order_events = [e for e in self.order_events if e.get("_received_at", datetime.min) > cutoff]
            self.delivery_events = [e for e in self.delivery_events if e.get("_received_at", datetime.min) > cutoff]
            self.rider_events = [e for e in self.rider_events if e.get("_received_at", datetime.min) > cutoff]
            self.restaurant_events = [e for e in self.restaurant_events if e.get("_received_at", datetime.min) > cutoff]
            self.payment_events = [e for e in self.payment_events if e.get("_received_at", datetime.min) > cutoff]

    # ===== 집계 메서드 =====

    def get_orders_by_region_hourly(self, hours: int = 6) -> dict[str, list[int]]:
        """지역별 시간당 주문량 (최근 N시간)"""
        now = datetime.now()
        result: dict[str, list[int]] = defaultdict(lambda: [0] * hours)

        with self._lock:
            for event in self.order_events:
                if event.get("eventType") != "ORDER_CREATED":
                    continue
                received = event.get("_received_at", datetime.min)
                region = event.get("region", "unknown")
                hours_ago = int((now - received).total_seconds() / 3600)
                if 0 <= hours_ago < hours:
                    result[region][hours_ago] += 1

        return dict(result)

    def get_cancels_by_region_1h(self) -> dict[str, int]:
        """지역별 최근 1시간 취소 수"""
        cutoff = datetime.now() - timedelta(hours=1)
        result: dict[str, int] = defaultdict(int)
        with self._lock:
            for event in self.order_events:
                if event.get("eventType") == "ORDER_CANCELLED" and event.get("_received_at", datetime.min) > cutoff:
                    result[event.get("region", "unknown")] += 1
        return dict(result)

    def get_orders_by_region_1h(self) -> dict[str, int]:
        """지역별 최근 1시간 주문 수"""
        cutoff = datetime.now() - timedelta(hours=1)
        result: dict[str, int] = defaultdict(int)
        with self._lock:
            for event in self.order_events:
                if event.get("eventType") == "ORDER_CREATED" and event.get("_received_at", datetime.min) > cutoff:
                    result[event.get("region", "unknown")] += 1
        return dict(result)

    def get_delivery_times_by_region(self, hours: int = 1) -> dict[str, list[float]]:
        """지역별 배달 완료 시간 목록 (최근 N시간)"""
        cutoff = datetime.now() - timedelta(hours=hours)
        result: dict[str, list[float]] = defaultdict(list)

        # 주문 생성 시각 추적
        order_created: dict[int, datetime] = {}
        with self._lock:
            for event in self.order_events:
                if event.get("eventType") == "ORDER_CREATED":
                    order_created[event.get("orderId")] = event.get("_received_at", datetime.now())

            for event in self.delivery_events:
                if (event.get("eventType") == "DELIVERY_COMPLETED"
                        and event.get("_received_at", datetime.min) > cutoff):
                    order_id = event.get("orderId")
                    region = event.get("region", "unknown")
                    if order_id in order_created:
                        duration = (event["_received_at"] - order_created[order_id]).total_seconds() / 60
                        result[region].append(duration)

        return dict(result)

    def get_rejections_by_restaurant_1h(self) -> dict[int, int]:
        """가맹점별 최근 1시간 주문 거부 수"""
        cutoff = datetime.now() - timedelta(hours=1)
        result: dict[int, int] = defaultdict(int)
        with self._lock:
            for event in self.restaurant_events:
                if (event.get("eventType") == "ORDER_REJECTED"
                        and event.get("_received_at", datetime.min) > cutoff):
                    result[event.get("restaurantId", 0)] += 1
        return dict(result)

    def get_rejections_by_region_1h(self) -> dict[str, int]:
        """지역별 최근 1시간 주문 거부 수"""
        cutoff = datetime.now() - timedelta(hours=1)
        result: dict[str, int] = defaultdict(int)
        with self._lock:
            for event in self.restaurant_events:
                if (event.get("eventType") == "ORDER_REJECTED"
                        and event.get("_received_at", datetime.min) > cutoff):
                    result[event.get("region", "unknown")] += 1
        return dict(result)

    def get_payment_failures_1h(self) -> dict[str, int]:
        """최근 1시간 결제 실패 수 (method별)"""
        cutoff = datetime.now() - timedelta(hours=1)
        result: dict[str, int] = defaultdict(int)
        with self._lock:
            for event in self.payment_events:
                if (event.get("eventType") == "PAYMENT_FAILED"
                        and event.get("_received_at", datetime.min) > cutoff):
                    result[event.get("method", "unknown")] += 1
        return dict(result)

    def get_payment_failure_count_1h(self) -> int:
        cutoff = datetime.now() - timedelta(hours=1)
        with self._lock:
            return sum(
                1 for e in self.payment_events
                if e.get("eventType") == "PAYMENT_FAILED" and e.get("_received_at", datetime.min) > cutoff
            )

    def get_available_riders_by_region(self) -> dict[str, int]:
        """지역별 가용 라이더 수"""
        result: dict[str, int] = defaultdict(int)
        with self._lock:
            for rider_id, info in self.rider_status.items():
                if info.get("status") == "AVAILABLE":
                    result[info.get("region", "unknown")] += 1
        return dict(result)

    def get_customer_order_history(self) -> dict[int, list[datetime]]:
        """고객별 주문 이력"""
        with self._lock:
            return dict(self.customer_orders)

    def get_all_regions(self) -> set[str]:
        """이벤트에서 등장한 모든 지역"""
        regions = set()
        with self._lock:
            for event in self.order_events:
                r = event.get("region")
                if r:
                    regions.add(r)
        return regions
