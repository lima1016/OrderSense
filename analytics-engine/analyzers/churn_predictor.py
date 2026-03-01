import logging
from datetime import datetime, timedelta

import config
from models.dto import AnomalyEvent, ChurnRiskCustomer
from storage.event_store import EventStore
from kafka.producer import KafkaAnomalyProducer

logger = logging.getLogger(__name__)


class ChurnPredictor:
    """고객 이탈 예측 - 주문 빈도 감소 기반"""

    def __init__(self, event_store: EventStore, producer: KafkaAnomalyProducer):
        self.event_store = event_store
        self.producer = producer

    def run(self) -> list[ChurnRiskCustomer]:
        """이탈 위험 고객 분석"""
        customer_orders = self.event_store.get_customer_order_history()
        at_risk_customers: list[ChurnRiskCustomer] = []

        now = datetime.now()
        recent_cutoff = now - timedelta(hours=12)
        older_cutoff = now - timedelta(hours=24)

        for customer_id, order_times in customer_orders.items():
            if len(order_times) < 3:
                continue

            # 최근 12시간 vs 이전 12시간 주문 수 비교
            recent_count = sum(1 for t in order_times if t > recent_cutoff)
            older_count = sum(1 for t in order_times if older_cutoff < t <= recent_cutoff)

            if older_count == 0:
                continue

            frequency_drop = (older_count - recent_count) / older_count

            if frequency_drop >= config.CHURN_FREQUENCY_DROP:
                risk_score = min(1.0, frequency_drop)
                at_risk_customers.append(ChurnRiskCustomer(
                    customer_id=customer_id,
                    total_orders=len(order_times),
                    recent_orders=recent_count,
                    order_frequency_drop=frequency_drop,
                    risk_score=risk_score,
                ))

        # 이탈 위험 고객이 일정 수 이상이면 anomaly 발행
        if len(at_risk_customers) >= 3:
            high_risk = [c for c in at_risk_customers if c.risk_score >= 0.7]
            if high_risk:
                anomaly = AnomalyEvent(
                    anomalyType="CUSTOMER_CHURN",
                    region=None,
                    description=f"이탈 위험 고객 {len(at_risk_customers)}명 감지 (고위험: {len(high_risk)}명)",
                    severity=min(1.0, len(high_risk) / 10),
                    metadata={
                        "totalAtRisk": len(at_risk_customers),
                        "highRiskCount": len(high_risk),
                        "customerIds": [c.customer_id for c in high_risk[:10]],
                    },
                    detectedAt=datetime.now().isoformat(),
                )
                self.producer.send_anomaly(anomaly)
                self.event_store.add_anomaly(anomaly.to_kafka_dict())
                logger.warning("고객 이탈 위험 탐지: %d명", len(at_risk_customers))

        return at_risk_customers
