#!/bin/bash
# ============================================================
# Kafka에 mock 이벤트를 주입하여 analytics engine에 데이터 공급
# + 이상 탐지가 발동하도록 특정 지역에 비정상 패턴 삽입
# ============================================================

KAFKA_CONTAINER="ordersense-kafka"
BROKER="localhost:9092"
NOW=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

echo "=== Kafka Mock Event Seeder ==="
echo "Timestamp: $NOW"

# ----------------------------------------
# 1. 주문 이벤트 (orders 토픽) - 10개 지역, 정상 + 비정상 패턴
# ----------------------------------------
echo "[1/6] Sending order events..."

REGIONS=("강남" "서초" "송파" "마포" "영등포" "종로" "강서" "관악" "성동" "용산")
STATUSES=("PENDING" "ACCEPTED" "PREPARING" "READY" "PICKED_UP" "DELIVERING" "DELIVERED")

# 정상 주문 이벤트: 각 지역 15~20건
for region in "${REGIONS[@]}"; do
  count=$((15 + RANDOM % 6))
  for ((j=1; j<=count; j++)); do
    order_id=$((RANDOM % 10000 + 1))
    customer_id=$((RANDOM % 200 + 1))
    restaurant_id=$((RANDOM % 50 + 1))
    amount=$((15000 + RANDOM % 30000))
    status=${STATUSES[$((RANDOM % ${#STATUSES[@]}))]}

    # 시간을 최근 1시간 내로 분산
    mins_ago=$((RANDOM % 60))
    ts=$(date -u -d "$mins_ago minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")

    echo "{\"eventType\":\"ORDER_CREATED\",\"orderId\":$order_id,\"customerId\":$customer_id,\"restaurantId\":$restaurant_id,\"region\":\"$region\",\"status\":\"$status\",\"totalAmount\":$amount,\"itemCount\":$((RANDOM % 4 + 1)),\"timestamp\":\"$ts\"}"
  done
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic orders 2>/dev/null

# 강남 지역 취소 급증 (이상 탐지 트리거: ORDER_DROP)
echo "[1.1] Injecting cancel spike in 강남..."
for ((j=1; j<=25; j++)); do
  order_id=$((RANDOM % 10000 + 10001))
  ts=$(date -u -d "$((RANDOM % 30)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")
  echo "{\"eventType\":\"ORDER_CANCELLED\",\"orderId\":$order_id,\"customerId\":$((RANDOM % 200 + 1)),\"restaurantId\":$((RANDOM % 8 + 1)),\"region\":\"강남\",\"status\":\"CANCELLED\",\"cancelReason\":\"고객 요청 취소\",\"totalAmount\":$((20000 + RANDOM % 20000)),\"itemCount\":2,\"timestamp\":\"$ts\"}"
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic orders 2>/dev/null

# ----------------------------------------
# 2. 배달 이벤트 (deliveries 토픽)
# ----------------------------------------
echo "[2/6] Sending delivery events..."

for region in "${REGIONS[@]}"; do
  for ((j=1; j<=10; j++)); do
    rider_id=$((RANDOM % 30 + 1))
    order_id=$((RANDOM % 10000 + 1))
    event_types=("RIDER_ASSIGNED" "RIDER_PICKED_UP" "DELIVERY_STARTED" "DELIVERY_COMPLETED")
    et=${event_types[$((RANDOM % ${#event_types[@]}))]}
    ts=$(date -u -d "$((RANDOM % 60)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")

    echo "{\"eventType\":\"$et\",\"riderId\":$rider_id,\"orderId\":$order_id,\"region\":\"$region\",\"lat\":37.$((4700 + RANDOM % 1000)),\"lng\":126.$((8400 + RANDOM % 2000)),\"timestamp\":\"$ts\"}"
  done
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic deliveries 2>/dev/null

# 송파 지역 배달 지연 패턴 (이상 탐지 트리거: DELIVERY_DELAY)
echo "[2.1] Injecting delivery delays in 송파..."
for ((j=1; j<=15; j++)); do
  rider_id=$((RANDOM % 5 + 7)) # 송파 라이더
  ts=$(date -u -d "$((RANDOM % 20)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")
  echo "{\"eventType\":\"DELIVERY_STARTED\",\"riderId\":$rider_id,\"orderId\":$((RANDOM % 10000 + 20001)),\"region\":\"송파\",\"lat\":37.5145,\"lng\":127.1058,\"timestamp\":\"$ts\"}"
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic deliveries 2>/dev/null

# ----------------------------------------
# 3. 라이더 이벤트 (riders 토픽)
# ----------------------------------------
echo "[3/6] Sending rider events..."

for ((i=1; i<=30; i++)); do
  region=${REGIONS[$(( (i-1) % 10 ))]}
  if [ $((i % 3)) -eq 0 ]; then
    status="BUSY"
  elif [ $((i % 7)) -eq 0 ]; then
    status="OFFLINE"
  else
    status="AVAILABLE"
  fi
  ts=$(date -u -d "$((RANDOM % 10)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")
  echo "{\"eventType\":\"LOCATION_UPDATED\",\"riderId\":$i,\"region\":\"$region\",\"lat\":37.$((4700 + RANDOM % 1000)),\"lng\":126.$((8400 + RANDOM % 2000)),\"status\":\"$status\",\"timestamp\":\"$ts\"}"
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic riders 2>/dev/null

# ----------------------------------------
# 4. 음식점 이벤트 (restaurants 토픽)
# ----------------------------------------
echo "[4/6] Sending restaurant events..."

for region in "${REGIONS[@]}"; do
  for ((j=1; j<=5; j++)); do
    restaurant_id=$((RANDOM % 50 + 1))
    event_types=("ORDER_ACCEPTED" "ORDER_PREPARING" "ORDER_READY")
    et=${event_types[$((RANDOM % ${#event_types[@]}))]}
    ts=$(date -u -d "$((RANDOM % 60)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")
    echo "{\"eventType\":\"$et\",\"restaurantId\":$restaurant_id,\"orderId\":$((RANDOM % 10000 + 1)),\"region\":\"$region\",\"prepTimeMinutes\":$((15 + RANDOM % 20)),\"timestamp\":\"$ts\"}"
  done
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic restaurants 2>/dev/null

# 마포 지역 거절 급증 (이상 탐지 트리거: REJECTION_SPIKE)
echo "[4.1] Injecting rejection spike in 마포..."
for ((j=1; j<=12; j++)); do
  ts=$(date -u -d "$((RANDOM % 30)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")
  echo "{\"eventType\":\"ORDER_REJECTED\",\"restaurantId\":$((RANDOM % 5 + 21)),\"orderId\":$((RANDOM % 10000 + 30001)),\"region\":\"마포\",\"timestamp\":\"$ts\"}"
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic restaurants 2>/dev/null

# ----------------------------------------
# 5. 결제 이벤트 (payments 토픽)
# ----------------------------------------
echo "[5/6] Sending payment events..."

for region in "${REGIONS[@]}"; do
  for ((j=1; j<=8; j++)); do
    order_id=$((RANDOM % 10000 + 1))
    amount=$((15000 + RANDOM % 30000))
    methods=("CARD" "CARD" "CARD" "KAKAO_PAY" "KAKAO_PAY" "NAVER_PAY" "CASH")
    method=${methods[$((RANDOM % ${#methods[@]}))]}
    ts=$(date -u -d "$((RANDOM % 60)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")
    echo "{\"eventType\":\"PAYMENT_COMPLETED\",\"orderId\":$order_id,\"amount\":$amount,\"method\":\"$method\",\"status\":\"COMPLETED\",\"transactionId\":\"TXN-MOCK-$order_id\",\"timestamp\":\"$ts\"}"
  done
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic payments 2>/dev/null

# 결제 실패 이벤트 추가
echo "[5.1] Injecting payment failures..."
for ((j=1; j<=8; j++)); do
  ts=$(date -u -d "$((RANDOM % 30)) minutes ago" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")
  reasons=("잔액 부족" "카드 한도 초과" "결제 시간 초과" "카드사 오류")
  reason=${reasons[$((RANDOM % ${#reasons[@]}))]}
  echo "{\"eventType\":\"PAYMENT_FAILED\",\"orderId\":$((RANDOM % 10000 + 40001)),\"amount\":$((20000 + RANDOM % 20000)),\"method\":\"CARD\",\"status\":\"FAILED\",\"failureReason\":\"$reason\",\"timestamp\":\"$ts\"}"
done | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic payments 2>/dev/null

# ----------------------------------------
# 6. 이상 탐지 이벤트 직접 주입 (anomalies 토픽)
# ----------------------------------------
echo "[6/6] Sending anomaly events..."

cat <<ANOMALIES | docker exec -i $KAFKA_CONTAINER kafka-console-producer --broker-list $BROKER --topic anomalies 2>/dev/null
{"anomalyType":"ORDER_DROP","region":"강남","description":"강남 지역 주문량 40% 급감 감지","severity":0.85,"metadata":{"current_orders":12,"avg_orders":28,"drop_rate":0.57},"detectedAt":"$NOW"}
{"anomalyType":"DELIVERY_DELAY","region":"송파","description":"송파 지역 평균 배달 시간 1.8배 초과","severity":0.72,"metadata":{"avg_delivery_min":52,"normal_avg":29,"delay_ratio":1.79},"detectedAt":"$NOW"}
{"anomalyType":"REJECTION_SPIKE","region":"마포","description":"마포 지역 음식점 주문 거절률 35% 돌파","severity":0.68,"metadata":{"rejection_count":12,"total_orders":34,"rejection_rate":0.35},"detectedAt":"$NOW"}
{"anomalyType":"CUSTOMER_CHURN","region":"영등포","description":"영등포 지역 고객 이탈 위험 감지 - 재주문율 급감","severity":0.55,"metadata":{"at_risk_customers":8,"frequency_drop":0.62},"detectedAt":"$NOW"}
{"anomalyType":"ORDER_DROP","region":"종로","description":"종로 지역 저녁 피크타임 주문량 30% 감소","severity":0.61,"metadata":{"current_orders":18,"avg_orders":26,"drop_rate":0.31},"detectedAt":"$NOW"}
{"anomalyType":"DELIVERY_DELAY","region":"강서","description":"강서 지역 배달 지연 다수 발생 - 라이더 부족 추정","severity":0.78,"metadata":{"avg_delivery_min":48,"normal_avg":28,"available_riders":1},"detectedAt":"$NOW"}
ANOMALIES

echo ""
echo "=== Complete! ==="
echo "- Order events: ~200 (including 25 cancel spike in 강남)"
echo "- Delivery events: ~115 (including delay pattern in 송파)"
echo "- Rider events: 30"
echo "- Restaurant events: ~62 (including 12 rejections in 마포)"
echo "- Payment events: ~88 (including 8 failures)"
echo "- Anomaly events: 6"
echo ""
echo "Wait 60s for analytics engine to process and detect anomalies..."
