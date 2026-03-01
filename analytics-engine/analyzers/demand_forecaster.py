import logging

import numpy as np

from models.dto import DemandForecast
from storage.event_store import EventStore

logger = logging.getLogger(__name__)


class DemandForecaster:
    """수요 예측 - 시간대별 이동평균 기반"""

    def __init__(self, event_store: EventStore):
        self.event_store = event_store

    def forecast(self, region: str | None = None) -> list[DemandForecast]:
        """지역별 다음 시간대 주문량 예측"""
        hourly_data = self.event_store.get_orders_by_region_hourly(hours=6)
        forecasts: list[DemandForecast] = []

        for r, counts in hourly_data.items():
            if region and r != region:
                continue

            current = counts[0]
            historical = counts[1:]

            if not historical or all(c == 0 for c in historical):
                forecasts.append(DemandForecast(
                    region=r,
                    current_hour_orders=current,
                    predicted_next_hour_orders=float(current),
                    trend="stable",
                    confidence=0.3,
                ))
                continue

            # 가중 이동평균 (최근 시간에 더 높은 가중치)
            weights = np.array([0.4, 0.3, 0.15, 0.1, 0.05][:len(historical)])
            weights = weights / weights.sum()
            weighted_avg = np.average(historical, weights=weights)

            # 추세 계산
            if len(historical) >= 2:
                trend_values = [current] + historical[:3]
                if trend_values[-1] > 0:
                    trend_ratio = trend_values[0] / trend_values[-1]
                    if trend_ratio > 1.15:
                        trend = "increasing"
                    elif trend_ratio < 0.85:
                        trend = "decreasing"
                    else:
                        trend = "stable"
                else:
                    trend = "stable"
            else:
                trend = "stable"

            # 예측: 가중 이동평균 + 추세 보정
            prediction = weighted_avg
            if trend == "increasing":
                prediction *= 1.1
            elif trend == "decreasing":
                prediction *= 0.9

            # 신뢰도: 데이터가 많고 분산이 작을수록 높음
            std = np.std(historical) if len(historical) > 1 else float(weighted_avg)
            mean = np.mean(historical)
            cv = std / mean if mean > 0 else 1.0
            confidence = max(0.1, min(0.95, 1.0 - cv))

            forecasts.append(DemandForecast(
                region=r,
                current_hour_orders=current,
                predicted_next_hour_orders=round(float(prediction), 1),
                trend=trend,
                confidence=round(confidence, 2),
            ))

        return forecasts

    def forecast_all_regions(self) -> list[DemandForecast]:
        return self.forecast(region=None)
