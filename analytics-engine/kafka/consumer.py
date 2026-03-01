import json
import logging
import threading

from confluent_kafka import Consumer, KafkaError

import config
from storage.event_store import EventStore

logger = logging.getLogger(__name__)

# 토픽 → EventStore 메서드 매핑
TOPIC_HANDLER_MAP = {
    "orders": "add_order_event",
    "deliveries": "add_delivery_event",
    "riders": "add_rider_event",
    "restaurants": "add_restaurant_event",
    "payments": "add_payment_event",
}


class KafkaEventConsumer:
    """5개 Kafka 토픽을 구독하여 EventStore에 저장"""

    def __init__(self, event_store: EventStore):
        self.event_store = event_store
        self._running = False
        self._thread: threading.Thread | None = None
        self._consumer: Consumer | None = None

    def start(self):
        if self._running:
            return
        self._running = True
        self._thread = threading.Thread(target=self._consume_loop, daemon=True)
        self._thread.start()
        logger.info("Kafka Consumer 시작 - 토픽: %s", config.CONSUME_TOPICS)

    def is_running(self) -> bool:
        return self._running

    def stop(self):
        self._running = False
        if self._thread:
            self._thread.join(timeout=5)
        if self._consumer:
            self._consumer.close()
        logger.info("Kafka Consumer 종료")

    def _consume_loop(self):
        self._consumer = Consumer({
            "bootstrap.servers": config.KAFKA_BOOTSTRAP_SERVERS,
            "group.id": config.KAFKA_GROUP_ID,
            "auto.offset.reset": "latest",
            "enable.auto.commit": True,
        })
        self._consumer.subscribe(config.CONSUME_TOPICS)

        while self._running:
            try:
                msg = self._consumer.poll(timeout=1.0)
                if msg is None:
                    continue
                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        continue
                    logger.error("Kafka 에러: %s", msg.error())
                    continue

                topic = msg.topic()
                value = msg.value()
                if value is None:
                    continue

                try:
                    event = json.loads(value.decode("utf-8"))
                except (json.JSONDecodeError, UnicodeDecodeError) as e:
                    logger.warning("JSON 파싱 실패 [%s]: %s", topic, e)
                    continue

                handler_name = TOPIC_HANDLER_MAP.get(topic)
                if handler_name:
                    handler = getattr(self.event_store, handler_name)
                    handler(event)
                    logger.debug("이벤트 수신 [%s]: %s", topic, event.get("eventType", "unknown"))

            except Exception as e:
                logger.error("Consumer 처리 중 오류: %s", e)
