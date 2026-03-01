import logging
from datetime import datetime

import numpy as np

import config
from models.dto import AnomalyEvent
from storage.event_store import EventStore
from kafka.producer import KafkaAnomalyProducer

logger = logging.getLogger(__name__)


class AnomalyDetector:
    """시계열 이상 탐지 - 주문량 급감/급증, 배달 시간 지연, 결제 실패율"""

    def __init__(self, event_store: EventStore, producer: KafkaAnomalyProducer):
        self.event_store = event_store
        self.producer = producer

    def run(self):
        """전체 이상 탐지 실행"""
        self._detect_order_volume_anomaly()
        self._detect_delivery_delay()
        self._detect_payment_failure_spike()

    def _detect_order_volume_anomaly(self):
        """지역별 주문량 급감/급증 탐지 (Z-score 기반)"""
        hourly_data = self.event_store.get_orders_by_region_hourly(hours=6)

        for region, counts in hourly_data.items():
            if len(counts) < 3:
                continue

            # counts[0] = 현재 시간, counts[1] = 1시간 전, ...
            current = counts[0]
            historical = counts[1:]

            if all(c == 0 for c in historical):
                continue

            mean = np.mean(historical)
            std = np.std(historical)

            if std == 0:
                if current == 0 and mean > 0:
                    # 갑자기 0으로 떨어짐
                    severity = min(1.0, mean / 10)
                    self._publish_anomaly(
                        anomaly_type="ORDER_DROP",
                        region=region,
                        description=f"{region} 주문량 완전 중단 (이전 평균: {mean:.0f}건/시간)",
                        severity=severity,
                        metadata={"currentRate": current, "previousMean": float(mean)},
                    )
                continue

            z_score = (current - mean) / std

            if z_score < -config.ANOMALY_Z_SCORE_THRESHOLD:
                # 주문량 급감
                drop_rate = (mean - current) / mean if mean > 0 else 0
                severity = min(1.0, abs(z_score) / 4)
                self._publish_anomaly(
                    anomaly_type="ORDER_DROP",
                    region=region,
                    description=f"{region} 주문량 {drop_rate*100:.0f}% 감소 (현재: {current}건, 평균: {mean:.0f}건/시간)",
                    severity=severity,
                    metadata={
                        "currentRate": current,
                        "previousMean": float(mean),
                        "zScore": float(z_score),
                        "dropRate": float(drop_rate),
                    },
                )
                logger.warning("주문량 급감 탐지: region=%s, z_score=%.2f", region, z_score)

            elif z_score > config.ANOMALY_Z_SCORE_THRESHOLD:
                # 주문량 급증 (참고 정보)
                surge_rate = (current - mean) / mean if mean > 0 else 0
                logger.info("주문량 급증 감지: region=%s, surge=%.0f%%, z_score=%.2f", region, surge_rate * 100, z_score)

    def _detect_delivery_delay(self):
        """배달 시간 지연 탐지"""
        recent_times = self.event_store.get_delivery_times_by_region(hours=1)
        historical_times = self.event_store.get_delivery_times_by_region(hours=6)

        for region, times in recent_times.items():
            if len(times) < 2:
                continue

            current_avg = np.mean(times)
            historical = historical_times.get(region, [])

            if len(historical) < 3:
                continue

            historical_avg = np.mean(historical)

            if historical_avg > 0 and current_avg > historical_avg * config.DELIVERY_DELAY_THRESHOLD:
                severity = min(1.0, (current_avg / historical_avg - 1) / 2)
                self._publish_anomaly(
                    anomaly_type="DELIVERY_DELAY",
                    region=region,
                    description=f"{region} 평균 배달 시간 {current_avg:.0f}분 (평소: {historical_avg:.0f}분)",
                    severity=severity,
                    metadata={
                        "currentAvgMinutes": float(current_avg),
                        "historicalAvgMinutes": float(historical_avg),
                        "increaseRatio": float(current_avg / historical_avg),
                        "sampleCount": len(times),
                    },
                )
                logger.warning("배달 시간 지연 탐지: region=%s, %.0f분 → %.0f분", region, historical_avg, current_avg)

    def _detect_payment_failure_spike(self):
        """결제 실패율 급증 탐지"""
        failure_count = self.event_store.get_payment_failure_count_1h()
        # 간단한 임계값 기반 탐지
        if failure_count >= 5:
            failures_by_method = self.event_store.get_payment_failures_1h()
            severity = min(1.0, failure_count / 20)
            self._publish_anomaly(
                anomaly_type="ORDER_DROP",  # 결제 실패도 주문 감소 원인
                region=None,
                description=f"결제 실패 급증: 최근 1시간 {failure_count}건",
                severity=severity,
                metadata={
                    "failureCount": failure_count,
                    "failuresByMethod": failures_by_method,
                    "cause": "PAYMENT_FAILURE",
                },
            )

    def _publish_anomaly(self, anomaly_type: str, region: str | None,
                         description: str, severity: float, metadata: dict):
        anomaly = AnomalyEvent(
            anomalyType=anomaly_type,
            region=region,
            description=description,
            severity=severity,
            metadata=metadata,
            detectedAt=datetime.now().isoformat(),
        )
        self.producer.send_anomaly(anomaly)
        self.event_store.add_anomaly(anomaly.to_kafka_dict())
