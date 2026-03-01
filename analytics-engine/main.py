import logging
from contextlib import asynccontextmanager

import uvicorn
from apscheduler.schedulers.background import BackgroundScheduler
from fastapi import FastAPI, Query

import config
from analyzers.anomaly_detector import AnomalyDetector
from analyzers.churn_predictor import ChurnPredictor
from analyzers.demand_forecaster import DemandForecaster
from analyzers.root_cause_classifier import RootCauseClassifier
from kafka.consumer import KafkaEventConsumer
from kafka.producer import KafkaAnomalyProducer
from storage.event_store import EventStore

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
logger = logging.getLogger(__name__)

# 공유 인스턴스
event_store = EventStore()
producer = KafkaAnomalyProducer()
consumer = KafkaEventConsumer(event_store)

anomaly_detector = AnomalyDetector(event_store, producer)
root_cause_classifier = RootCauseClassifier(event_store)
churn_predictor = ChurnPredictor(event_store, producer)
demand_forecaster = DemandForecaster(event_store)

scheduler = BackgroundScheduler()


def run_analysis():
    """주기적 분석 실행"""
    try:
        logger.info("=== 분석 사이클 시작 ===")

        # 1. 이상 탐지
        anomaly_detector.run()

        # 2. 이상 발생 지역에 대해 원인 분류
        regions = event_store.get_all_regions()
        for region in regions:
            root_cause_classifier.classify(region)

        # 3. 고객 이탈 예측
        churn_predictor.run()

        # 4. 오래된 이벤트 정리
        event_store.cleanup_old_events()

        logger.info("=== 분석 사이클 완료 ===")
    except Exception as e:
        logger.error("분석 사이클 오류: %s", e, exc_info=True)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Analytics Engine 시작 (port=%d)", config.API_PORT)
    consumer.start()
    scheduler.add_job(run_analysis, "interval", seconds=config.ANALYSIS_INTERVAL_SECONDS)
    scheduler.start()
    logger.info("스케줄러 시작: %d초 주기", config.ANALYSIS_INTERVAL_SECONDS)

    yield

    # Shutdown
    logger.info("Analytics Engine 종료 중...")
    scheduler.shutdown(wait=False)
    consumer.stop()


app = FastAPI(
    title="OrderSense Analytics Engine",
    description="실시간 주문 데이터 분석 및 이상 탐지 엔진",
    version="1.0.0",
    lifespan=lifespan,
)


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "analytics-engine",
        "kafkaConsumer": "running" if consumer.is_running() else "stopped",
        "scheduler": "running" if scheduler.running else "stopped",
    }


@app.get("/api/analytics/stats")
def get_stats():
    """현재 집계 메트릭"""
    orders_hourly = event_store.get_orders_by_region_hourly(hours=6)
    cancels = event_store.get_cancels_by_region_1h()
    available_riders = event_store.get_available_riders_by_region()
    payment_failures = event_store.get_payment_failure_count_1h()

    # 지역별 현재 시간 주문량
    current_orders = {region: counts[0] for region, counts in orders_hourly.items()}

    return {
        "currentOrdersByRegion": current_orders,
        "cancelsByRegion": cancels,
        "availableRidersByRegion": available_riders,
        "paymentFailures1h": payment_failures,
        "totalRegions": len(event_store.get_all_regions()),
        "eventCounts": {
            "orders": len(event_store.order_events),
            "deliveries": len(event_store.delivery_events),
            "riders": len(event_store.rider_events),
            "restaurants": len(event_store.restaurant_events),
            "payments": len(event_store.payment_events),
        },
    }


@app.get("/api/analytics/anomalies")
def get_anomalies(limit: int = Query(default=50, ge=1, le=200)):
    """최근 탐지된 이상 목록"""
    anomalies = event_store.get_recent_anomalies(limit=limit)
    return {"count": len(anomalies), "anomalies": anomalies}


@app.get("/api/analytics/demand")
def get_demand_forecast(region: str | None = Query(default=None)):
    """수요 예측"""
    forecasts = demand_forecaster.forecast(region=region)
    return {
        "count": len(forecasts),
        "forecasts": [f.model_dump() for f in forecasts],
    }


@app.get("/api/analytics/churn")
def get_churn_risk():
    """이탈 위험 고객 목록"""
    at_risk = churn_predictor.run()
    return {
        "count": len(at_risk),
        "atRiskCustomers": [c.model_dump() for c in at_risk],
    }


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=config.API_HOST,
        port=config.API_PORT,
        reload=False,
    )
