import logging

from storage.event_store import EventStore

logger = logging.getLogger(__name__)


class RootCauseClassifier:
    """이상 탐지 시 원인 분류"""

    def __init__(self, event_store: EventStore):
        self.event_store = event_store

    def classify(self, region: str) -> dict:
        """특정 지역의 이상 원인을 분류하여 반환"""
        causes = []
        details = {}

        # 1. 라이더 가용성 체크
        available_riders = self.event_store.get_available_riders_by_region()
        rider_count = available_riders.get(region, 0)
        details["availableRiders"] = rider_count
        if rider_count < 3:
            causes.append("RIDER_SHORTAGE")
            logger.info("원인 분류 [%s]: 라이더 부족 (가용: %d명)", region, rider_count)

        # 2. 가맹점 거부율 체크
        rejections = self.event_store.get_rejections_by_region_1h()
        orders = self.event_store.get_orders_by_region_1h()
        rejection_count = rejections.get(region, 0)
        order_count = orders.get(region, 0)
        details["rejectionCount"] = rejection_count
        details["orderCount"] = order_count

        if order_count > 0 and rejection_count / order_count > 0.2:
            causes.append("HIGH_REJECTION_RATE")
            logger.info("원인 분류 [%s]: 높은 거부율 (%d/%d)", region, rejection_count, order_count)

        # 3. 결제 실패 체크
        payment_failures = self.event_store.get_payment_failure_count_1h()
        details["paymentFailures"] = payment_failures
        if payment_failures > 5:
            causes.append("PAYMENT_FAILURE")
            logger.info("원인 분류 [%s]: 결제 실패 다수 (%d건)", region, payment_failures)

        if not causes:
            causes.append("UNKNOWN")

        primary_cause = causes[0]
        details["allCauses"] = causes

        return {
            "cause": primary_cause,
            "allCauses": causes,
            "details": details,
        }
