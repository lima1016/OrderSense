"""
Elasticsearch에 10,000건 주문 mock 데이터 삽입 스크립트
사용: python scripts/seed-es-orders.py
"""

import json
import random
import urllib.request
from datetime import datetime, timedelta

ES_URL = "http://localhost:9200"
INDEX = "ordersense-order-search"

# ── Mock 데이터 소스 ──

REGIONS = ["강남", "홍대", "건대", "신촌", "이태원", "잠실", "여의도", "마포", "성수", "송파"]

CUSTOMER_NAMES = [
    "김민수", "이서연", "박지훈", "최유진", "정현우", "강예린", "조승현", "윤하은",
    "임도윤", "한소희", "오재원", "신미래", "황준혁", "문채원", "배성민", "류지아",
    "송태양", "장하늘", "권도현", "전소연", "서민재", "노유정", "안지호", "구하린",
    "홍석우", "양서윤", "유정민", "남지현", "김태희", "이준호", "박서영", "최동현",
    "정수아", "강민호", "조예은", "윤재혁", "임서현", "한동욱", "오미영", "신재민",
]

ADDRESSES = {
    "강남": ["강남구 역삼동 123-4", "강남구 삼성동 45-6", "강남구 논현동 78-9", "강남구 대치동 234-5", "강남구 청담동 67-8"],
    "홍대": ["마포구 서교동 345-6", "마포구 동교동 67-8", "마포구 합정동 12-3", "마포구 상수동 45-6"],
    "건대": ["광진구 화양동 12-3", "광진구 자양동 45-6", "광진구 구의동 78-9", "광진구 능동 34-5"],
    "신촌": ["서대문구 창천동 56-7", "서대문구 대현동 89-1", "서대문구 신촌동 23-4"],
    "이태원": ["용산구 이태원동 34-5", "용산구 한남동 67-8", "용산구 녹사평대로 12-3"],
    "잠실": ["송파구 잠실동 45-6", "송파구 신천동 78-9", "송파구 석촌동 23-4", "송파구 방이동 56-7"],
    "여의도": ["영등포구 여의도동 12-3", "영등포구 당산동 45-6", "영등포구 문래동 78-9"],
    "마포": ["마포구 공덕동 34-5", "마포구 도화동 67-8", "마포구 용강동 12-3"],
    "성수": ["성동구 성수동 45-6", "성동구 금호동 78-9", "성동구 행당동 23-4"],
    "송파": ["송파구 가락동 56-7", "송파구 문정동 89-1", "송파구 장지동 34-5"],
}

MENU_ITEMS = [
    ("김치찌개", 8000), ("된장찌개", 8000), ("순두부찌개", 8500),
    ("떡볶이", 5000), ("김밥", 3500), ("라면", 4500),
    ("치킨", 18000), ("양념치킨", 19000), ("간장치킨", 19000), ("마늘치킨", 20000),
    ("피자", 22000), ("페퍼로니 피자", 20000), ("콤비네이션 피자", 24000),
    ("짜장면", 7000), ("짬뽕", 8000), ("탕수육", 15000), ("볶음밥", 8000),
    ("돈까스", 9000), ("치즈돈까스", 11000), ("카레라이스", 8500),
    ("비빔밥", 9000), ("불고기", 12000), ("갈비탕", 13000), ("삼겹살", 14000),
    ("냉면", 10000), ("칼국수", 9000), ("수제비", 8500),
    ("샌드위치", 6500), ("햄버거", 8000), ("감자튀김", 4000),
    ("파스타", 12000), ("리조또", 13000), ("스테이크", 25000),
    ("초밥 세트", 15000), ("사시미", 20000), ("우동", 7500),
    ("떡갈비", 11000), ("제육볶음", 9000), ("오므라이스", 8500),
    ("아메리카노", 4500), ("카페라떼", 5000), ("녹차라떼", 5500),
]

STATUSES = ["PENDING", "ACCEPTED", "PREPARING", "READY", "PICKED_UP", "DELIVERING", "DELIVERED", "CANCELLED"]
STATUS_WEIGHTS = [5, 5, 8, 5, 5, 10, 55, 7]  # DELIVERED가 가장 많음

CANCEL_REASONS = [
    "고객 요청 취소", "배달 지연으로 취소", "재료 소진", "가게 사정으로 취소",
    "결제 오류", "주소 오류", "메뉴 변경 불가", "영업 시간 종료",
]

LAT_RANGE = (37.48, 37.58)  # 서울 위도 범위
LNG_RANGE = (126.90, 127.10)  # 서울 경도 범위


def generate_order(order_id: int, base_time: datetime) -> dict:
    region = random.choice(REGIONS)
    customer_id = random.randint(1, 200)
    customer_name = random.choice(CUSTOMER_NAMES)
    restaurant_id = random.randint(1, 50)

    # 주문 시간: 최근 30일 내 랜덤
    order_time = base_time - timedelta(
        days=random.randint(0, 30),
        hours=random.randint(0, 23),
        minutes=random.randint(0, 59),
    )

    # 상태 결정
    status = random.choices(STATUSES, weights=STATUS_WEIGHTS, k=1)[0]

    # 주문 항목 (1~4개)
    num_items = random.randint(1, 4)
    selected_menus = random.sample(MENU_ITEMS, num_items)
    items = []
    total_amount = 0
    for i, (name, price) in enumerate(selected_menus):
        qty = random.randint(1, 3)
        item_total = price * qty
        total_amount += item_total
        items.append({
            "id": order_id * 10 + i,
            "menuItemName": name,
            "quantity": qty,
            "unitPrice": price,
            "totalPrice": item_total,
        })

    # 타임라인 생성
    timestamps = {"orderTime": order_time.strftime("%Y-%m-%dT%H:%M:%S")}
    accepted_time = prep_time = ready_time = pickup_time = delivered_time = cancelled_time = None
    cancel_reason = None
    estimated_min = random.randint(25, 55)
    actual_min = None
    rider_id = None

    if status == "CANCELLED":
        cancel_reason = random.choice(CANCEL_REASONS)
        cancel_offset = random.randint(1, 20)
        cancelled_time = order_time + timedelta(minutes=cancel_offset)
        timestamps["cancelledTime"] = cancelled_time.strftime("%Y-%m-%dT%H:%M:%S")
        # 취소 전까지 진행된 단계
        if cancel_offset > 3:
            accepted_time = order_time + timedelta(minutes=random.randint(1, 3))
            timestamps["acceptedTime"] = accepted_time.strftime("%Y-%m-%dT%H:%M:%S")
    elif status != "PENDING":
        accepted_time = order_time + timedelta(minutes=random.randint(1, 5))
        timestamps["acceptedTime"] = accepted_time.strftime("%Y-%m-%dT%H:%M:%S")

        if status not in ("ACCEPTED",):
            prep_start = accepted_time + timedelta(minutes=random.randint(1, 3))
            timestamps["prepStartTime"] = prep_start.strftime("%Y-%m-%dT%H:%M:%S")

            if status not in ("PREPARING",):
                ready = prep_start + timedelta(minutes=random.randint(5, 20))
                timestamps["readyTime"] = ready.strftime("%Y-%m-%dT%H:%M:%S")

                if status not in ("READY",):
                    rider_id = random.randint(1, 30)
                    pickup = ready + timedelta(minutes=random.randint(1, 10))
                    timestamps["pickupTime"] = pickup.strftime("%Y-%m-%dT%H:%M:%S")

                    if status not in ("PICKED_UP",):
                        if status == "DELIVERED":
                            deliver = pickup + timedelta(minutes=random.randint(5, 30))
                            timestamps["deliveredTime"] = deliver.strftime("%Y-%m-%dT%H:%M:%S")
                            actual_min = int((deliver - order_time).total_seconds() / 60)

    address = random.choice(ADDRESSES.get(region, ["서울시 " + region]))

    doc = {
        "id": order_id,
        "orderId": order_id,
        "customerId": customer_id,
        "customerName": customer_name,
        "restaurantId": restaurant_id,
        "riderId": rider_id,
        "status": status,
        "totalAmount": total_amount,
        "deliveryAddress": f"서울특별시 {address}",
        "deliveryLat": round(random.uniform(*LAT_RANGE), 8),
        "deliveryLng": round(random.uniform(*LNG_RANGE), 8),
        "region": region,
        "cancelReason": cancel_reason,
        "estimatedDeliveryMinutes": estimated_min,
        "actualDeliveryMinutes": actual_min,
        "items": items,
        "createdAt": timestamps["orderTime"],
    }
    doc.update(timestamps)

    # null 값인 타임스탬프 필드 제거 (ES에서 null date 파싱 이슈 방지)
    for key in ["acceptedTime", "prepStartTime", "readyTime", "pickupTime", "deliveredTime", "cancelledTime"]:
        if key not in doc:
            doc[key] = None

    return doc


def bulk_insert(orders: list[dict], batch_size: int = 500):
    """ES Bulk API로 일괄 삽입"""
    total = len(orders)
    inserted = 0

    for start in range(0, total, batch_size):
        batch = orders[start:start + batch_size]
        lines = []
        for doc in batch:
            meta = json.dumps({"index": {"_index": INDEX, "_id": str(doc["id"])}})
            body = json.dumps(doc, ensure_ascii=False)
            lines.append(meta)
            lines.append(body)

        bulk_body = "\n".join(lines) + "\n"

        req = urllib.request.Request(
            f"{ES_URL}/_bulk",
            data=bulk_body.encode("utf-8"),
            headers={"Content-Type": "application/x-ndjson"},
            method="POST",
        )

        try:
            with urllib.request.urlopen(req) as resp:
                result = json.loads(resp.read())
                if result.get("errors"):
                    # 첫 번째 에러만 출력
                    for item in result["items"]:
                        if "error" in item.get("index", {}):
                            print(f"  ERROR: {item['index']['error']['reason']}")
                            break
                else:
                    inserted += len(batch)
                    print(f"  [{inserted}/{total}] 삽입 완료")
        except Exception as e:
            print(f"  Bulk insert 실패: {e}")
            return

    print(f"\n총 {inserted}건 ES 인덱싱 완료!")


def delete_index():
    """기존 인덱스 삭제"""
    req = urllib.request.Request(f"{ES_URL}/{INDEX}", method="DELETE")
    try:
        with urllib.request.urlopen(req) as resp:
            print(f"기존 인덱스 삭제: {resp.read().decode()}")
    except urllib.error.HTTPError:
        print("기존 인덱스 없음 (skip)")


def create_index():
    """인덱스 생성 (매핑 설정)"""
    mapping = {
        "mappings": {
            "properties": {
                "id": {"type": "long"},
                "orderId": {"type": "long"},
                "customerId": {"type": "long"},
                "customerName": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "restaurantId": {"type": "long"},
                "riderId": {"type": "long"},
                "status": {"type": "keyword"},
                "totalAmount": {"type": "long"},
                "deliveryAddress": {"type": "text"},
                "deliveryLat": {"type": "double"},
                "deliveryLng": {"type": "double"},
                "region": {"type": "keyword"},
                "orderTime": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
                "acceptedTime": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
                "prepStartTime": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
                "readyTime": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
                "pickupTime": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
                "deliveredTime": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
                "cancelledTime": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
                "cancelReason": {"type": "text"},
                "estimatedDeliveryMinutes": {"type": "integer"},
                "actualDeliveryMinutes": {"type": "integer"},
                "items": {
                    "type": "nested",
                    "properties": {
                        "id": {"type": "long"},
                        "menuItemName": {"type": "text"},
                        "quantity": {"type": "integer"},
                        "unitPrice": {"type": "long"},
                        "totalPrice": {"type": "long"},
                    },
                },
                "createdAt": {"type": "date", "format": "yyyy-MM-dd'T'HH:mm:ss||epoch_millis"},
            }
        }
    }

    req = urllib.request.Request(
        f"{ES_URL}/{INDEX}",
        data=json.dumps(mapping).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="PUT",
    )
    try:
        with urllib.request.urlopen(req) as resp:
            print(f"인덱스 생성: {resp.read().decode()}")
    except Exception as e:
        print(f"인덱스 생성 실패: {e}")


def main():
    print("=" * 50)
    print("OrderSense ES Mock 데이터 생성기")
    print("=" * 50)

    count = 10000
    now = datetime.now()

    print(f"\n1. 기존 ES 인덱스 ({INDEX}) 삭제...")
    delete_index()

    print(f"\n2. 새 인덱스 생성 (매핑 포함)...")
    create_index()

    print(f"\n3. {count}건 mock 데이터 생성 중...")
    orders = [generate_order(i + 1, now) for i in range(count)]

    # 통계 출력
    status_counts = {}
    for o in orders:
        status_counts[o["status"]] = status_counts.get(o["status"], 0) + 1
    print("\n  상태별 분포:")
    for s, c in sorted(status_counts.items(), key=lambda x: -x[1]):
        print(f"    {s}: {c}건")

    region_counts = {}
    for o in orders:
        region_counts[o["region"]] = region_counts.get(o["region"], 0) + 1
    print("\n  지역별 분포:")
    for r, c in sorted(region_counts.items(), key=lambda x: -x[1]):
        print(f"    {r}: {c}건")

    print(f"\n4. ES Bulk Insert 시작...")
    bulk_insert(orders)

    print("\n완료!")


if __name__ == "__main__":
    main()
