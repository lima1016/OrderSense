import json
import logging

from confluent_kafka import Producer

import config
from models.dto import AnomalyEvent

logger = logging.getLogger(__name__)


class KafkaAnomalyProducer:
    """이상 탐지 결과를 anomalies 토픽으로 발행"""

    def __init__(self):
        self._producer = Producer({
            "bootstrap.servers": config.KAFKA_BOOTSTRAP_SERVERS,
        })

    def send_anomaly(self, anomaly: AnomalyEvent):
        try:
            value = json.dumps(anomaly.to_kafka_dict(), default=str).encode("utf-8")
            self._producer.produce(
                topic=config.ANOMALY_TOPIC,
                value=value,
                callback=self._delivery_callback,
            )
            self._producer.flush(timeout=5)
            logger.info(
                "이상 탐지 이벤트 발행: type=%s, region=%s, severity=%.2f",
                anomaly.anomalyType, anomaly.region, anomaly.severity,
            )
        except Exception as e:
            logger.error("이상 탐지 이벤트 발행 실패: %s", e)

    @staticmethod
    def _delivery_callback(err, msg):
        if err:
            logger.error("Kafka 전송 실패: %s", err)
        else:
            logger.debug("Kafka 전송 성공: partition=%s, offset=%s", msg.partition(), msg.offset())
