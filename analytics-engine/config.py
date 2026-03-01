import os

# Kafka
KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
KAFKA_GROUP_ID = "analytics-engine-group"

# 소비할 토픽
CONSUME_TOPICS = ["orders", "deliveries", "riders", "restaurants", "payments"]

# 발행할 토픽
ANOMALY_TOPIC = "anomalies"

# 분석 설정
ANALYSIS_INTERVAL_SECONDS = 60  # 분석 주기 (초)
EVENT_RETENTION_HOURS = 24      # 이벤트 보관 시간
ANOMALY_Z_SCORE_THRESHOLD = 2.0 # Z-score 이상 탐지 임계값
DELIVERY_DELAY_THRESHOLD = 1.5  # 평균 대비 1.5배 이상이면 지연
CHURN_FREQUENCY_DROP = 0.5      # 주문 빈도 50% 이상 감소 시 이탈 위험
REJECTION_RATE_THRESHOLD = 0.2  # 거부율 20% 이상이면 이상

# 서버
API_HOST = "0.0.0.0"
API_PORT = 8086
