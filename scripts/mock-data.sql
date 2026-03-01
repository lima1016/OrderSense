-- ============================================================
-- OrderSense Mock Data Generator
-- 고객 200명, 음식점 50개, 메뉴 ~250개, 라이더 30명
-- 주문 10,000건, 주문항목 ~25,000건, 결제 10,000건, 액션로그 200건
-- ============================================================

BEGIN;

-- 해시 헬퍼 함수 (deterministic pseudo-random)
CREATE OR REPLACE FUNCTION h(val int) RETURNS int AS $$
  SELECT ('x' || substr(md5(val::text), 1, 8))::bit(32)::int;
$$ LANGUAGE sql IMMUTABLE;

-- 시퀀스 리셋을 위해 기존 데이터 정리
TRUNCATE tb_action_logs, order_items, tb_payments, tb_orders, tb_menus, tb_restaurants, tb_riders, tb_customers RESTART IDENTITY CASCADE;

-- ============================================================
-- 1. 고객 200명
-- ============================================================
INSERT INTO tb_customers (email, password, name, phone, address, role, created_at, updated_at)
SELECT
  'user' || i || '@ordersense.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: 123456
  (ARRAY['김민수','이서연','박지호','최유진','정현우','강수빈','조태현','윤예린','장동혁','한소영',
         '임재현','오하늘','신민지','권도윤','송지원','류승현','배은서','홍성민','문예진','양준혁',
         '구하린','노태우','남지현','천승우','하예슬','전민호','서유나','안동현','유서현','고준영'])[((i-1) % 30) + 1],
  '010-' || LPAD((1000 + (i * 7) % 9000)::text, 4, '0') || '-' || LPAD((1000 + (i * 13) % 9000)::text, 4, '0'),
  (ARRAY['강남구','서초구','송파구','마포구','영등포구','종로구','강서구','관악구','성동구','용산구'])[((i-1) % 10) + 1] || ' ' ||
  (ARRAY['대로','로','길'])[((i-1) % 3) + 1] || ' ' || (i * 3 % 200 + 1)::text,
  CASE WHEN i <= 5 THEN 'ADMIN' ELSE 'USER' END,
  NOW() - (INTERVAL '90 days') + (INTERVAL '1 day' * (i % 90)),
  NOW()
FROM generate_series(1, 200) AS i;

-- ============================================================
-- 2. 음식점 50개
-- ============================================================
INSERT INTO tb_restaurants (name, address, region, phone, category, status, avg_prep_time, display_rank, created_at, updated_at)
VALUES
-- 강남 (8개)
('강남 본가 설렁탕', '강남구 강남대로 328', '강남', '02-555-0001', '한식', 'OPEN', 20, 1, NOW()-INTERVAL '180 days', NOW()),
('스시 오마카세 강남', '강남구 테헤란로 112', '강남', '02-555-0002', '일식', 'OPEN', 30, 2, NOW()-INTERVAL '180 days', NOW()),
('강남 치킨마을', '강남구 역삼로 45', '강남', '02-555-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '180 days', NOW()),
('파스타 하우스 강남', '강남구 논현로 67', '강남', '02-555-0004', '양식', 'OPEN', 25, 4, NOW()-INTERVAL '180 days', NOW()),
('짬뽕 대왕 강남', '강남구 봉은사로 23', '강남', '02-555-0005', '중식', 'OPEN', 20, 5, NOW()-INTERVAL '180 days', NOW()),
('피자 나라 강남', '강남구 선릉로 89', '강남', '02-555-0006', '피자', 'OPEN', 30, 6, NOW()-INTERVAL '180 days', NOW()),
('분식 천국 강남', '강남구 학동로 34', '강남', '02-555-0007', '분식', 'OPEN', 15, 7, NOW()-INTERVAL '180 days', NOW()),
('버거 팩토리 강남', '강남구 압구정로 56', '강남', '02-555-0008', '양식', 'CLOSED', 20, 8, NOW()-INTERVAL '180 days', NOW()),
-- 서초 (6개)
('서초 곰탕집', '서초구 서초대로 250', '서초', '02-556-0001', '한식', 'OPEN', 15, 1, NOW()-INTERVAL '150 days', NOW()),
('서초 돈까스 명가', '서초구 반포대로 78', '서초', '02-556-0002', '일식', 'OPEN', 20, 2, NOW()-INTERVAL '150 days', NOW()),
('BBQ 치킨 서초', '서초구 방배로 45', '서초', '02-556-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '150 days', NOW()),
('서초 짜장면 하우스', '서초구 잠원로 12', '서초', '02-556-0004', '중식', 'OPEN', 15, 4, NOW()-INTERVAL '150 days', NOW()),
('피자 알볼로 서초', '서초구 사평대로 67', '서초', '02-556-0005', '피자', 'OPEN', 30, 5, NOW()-INTERVAL '150 days', NOW()),
('떡볶이 타운 서초', '서초구 효령로 89', '서초', '02-556-0006', '분식', 'OPEN', 15, 6, NOW()-INTERVAL '150 days', NOW()),
-- 송파 (6개)
('송파 갈비 한우', '송파구 올림픽로 300', '송파', '02-557-0001', '한식', 'OPEN', 25, 1, NOW()-INTERVAL '120 days', NOW()),
('롯데월드 스시', '송파구 잠실로 50', '송파', '02-557-0002', '일식', 'OPEN', 30, 2, NOW()-INTERVAL '120 days', NOW()),
('교촌치킨 송파', '송파구 백제고분로 23', '송파', '02-557-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '120 days', NOW()),
('마라탕 송파점', '송파구 송파대로 78', '송파', '02-557-0004', '중식', 'OPEN', 20, 4, NOW()-INTERVAL '120 days', NOW()),
('도미노 피자 송파', '송파구 가락로 45', '송파', '02-557-0005', '피자', 'OPEN', 30, 5, NOW()-INTERVAL '120 days', NOW()),
('김밥 나라 송파', '송파구 문정로 12', '송파', '02-557-0006', '분식', 'SUSPENDED', 10, 6, NOW()-INTERVAL '120 days', NOW()),
-- 마포 (5개)
('마포 숯불갈비', '마포구 마포대로 200', '마포', '02-558-0001', '한식', 'OPEN', 25, 1, NOW()-INTERVAL '100 days', NOW()),
('홍대 라멘집', '마포구 홍익로 34', '마포', '02-558-0002', '일식', 'OPEN', 20, 2, NOW()-INTERVAL '100 days', NOW()),
('네네치킨 마포', '마포구 양화로 67', '마포', '02-558-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '100 days', NOW()),
('마포 양꼬치', '마포구 와우산로 89', '마포', '02-558-0004', '중식', 'OPEN', 20, 4, NOW()-INTERVAL '100 days', NOW()),
('브리오슈 버거 마포', '마포구 어울마당로 12', '마포', '02-558-0005', '양식', 'OPEN', 25, 5, NOW()-INTERVAL '100 days', NOW()),
-- 영등포 (5개)
('영등포 백반집', '영등포구 영등포로 150', '영등포', '02-559-0001', '한식', 'OPEN', 15, 1, NOW()-INTERVAL '90 days', NOW()),
('우동 하나 영등포', '영등포구 여의대로 78', '영등포', '02-559-0002', '일식', 'OPEN', 15, 2, NOW()-INTERVAL '90 days', NOW()),
('굽네치킨 영등포', '영등포구 당산로 45', '영등포', '02-559-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '90 days', NOW()),
('영등포 볶음밥', '영등포구 선유로 23', '영등포', '02-559-0004', '중식', 'OPEN', 15, 4, NOW()-INTERVAL '90 days', NOW()),
('파파존스 영등포', '영등포구 국회대로 56', '영등포', '02-559-0005', '피자', 'OPEN', 30, 5, NOW()-INTERVAL '90 days', NOW()),
-- 종로 (4개)
('종로 냉면집', '종로구 종로 100', '종로', '02-560-0001', '한식', 'OPEN', 15, 1, NOW()-INTERVAL '80 days', NOW()),
('종로 초밥왕', '종로구 인사동길 34', '종로', '02-560-0002', '일식', 'OPEN', 25, 2, NOW()-INTERVAL '80 days', NOW()),
('BHC 치킨 종로', '종로구 돈화문로 67', '종로', '02-560-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '80 days', NOW()),
('종로 탕수육', '종로구 삼봉로 12', '종로', '02-560-0004', '중식', 'OPEN', 20, 4, NOW()-INTERVAL '80 days', NOW()),
-- 강서 (4개)
('강서 김치찌개', '강서구 공항대로 200', '강서', '02-561-0001', '한식', 'OPEN', 15, 1, NOW()-INTERVAL '70 days', NOW()),
('강서 카레집', '강서구 화곡로 45', '강서', '02-561-0002', '일식', 'OPEN', 20, 2, NOW()-INTERVAL '70 days', NOW()),
('호식이 두마리 강서', '강서구 등촌로 78', '강서', '02-561-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '70 days', NOW()),
('강서 피자헛', '강서구 마곡중앙로 23', '강서', '02-561-0004', '피자', 'OPEN', 30, 4, NOW()-INTERVAL '70 days', NOW()),
-- 관악 (4개)
('관악 순대국', '관악구 관악로 150', '관악', '02-562-0001', '한식', 'OPEN', 15, 1, NOW()-INTERVAL '60 days', NOW()),
('관악 텐동집', '관악구 신림로 34', '관악', '02-562-0002', '일식', 'OPEN', 20, 2, NOW()-INTERVAL '60 days', NOW()),
('푸라닭 관악', '관악구 남부순환로 67', '관악', '02-562-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '60 days', NOW()),
('관악 분식왕', '관악구 봉천로 89', '관악', '02-562-0004', '분식', 'OPEN', 10, 4, NOW()-INTERVAL '60 days', NOW()),
-- 성동 (4개)
('성동 육개장', '성동구 왕십리로 100', '성동', '02-563-0001', '한식', 'OPEN', 20, 1, NOW()-INTERVAL '50 days', NOW()),
('성수동 스시바', '성동구 성수이로 45', '성동', '02-563-0002', '일식', 'OPEN', 30, 2, NOW()-INTERVAL '50 days', NOW()),
('60계 치킨 성동', '성동구 마장로 78', '성동', '02-563-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '50 days', NOW()),
('성동 짬뽕집', '성동구 행당로 23', '성동', '02-563-0004', '중식', 'OPEN', 15, 4, NOW()-INTERVAL '50 days', NOW()),
-- 용산 (4개)
('이태원 불고기', '용산구 이태원로 200', '용산', '02-564-0001', '한식', 'OPEN', 25, 1, NOW()-INTERVAL '40 days', NOW()),
('용산 라멘 골목', '용산구 한강대로 78', '용산', '02-564-0002', '일식', 'OPEN', 20, 2, NOW()-INTERVAL '40 days', NOW()),
('페리카나 용산', '용산구 원효로 45', '용산', '02-564-0003', '치킨', 'OPEN', 25, 3, NOW()-INTERVAL '40 days', NOW()),
('용산 양식당', '용산구 녹사평대로 56', '용산', '02-564-0004', '양식', 'OPEN', 30, 4, NOW()-INTERVAL '40 days', NOW());

-- ============================================================
-- 3. 메뉴 (음식점별 5개씩 = 250개)
-- ============================================================
INSERT INTO tb_menus (restaurant_id, name, price, available)
SELECT
  r.id,
  m.menu_name,
  m.menu_price,
  CASE WHEN random() > 0.1 THEN true ELSE false END
FROM tb_restaurants r
CROSS JOIN LATERAL (
  SELECT unnest(
    CASE r.category
      WHEN '한식' THEN ARRAY['된장찌개','김치찌개','불고기','비빔밥','제육볶음']
      WHEN '일식' THEN ARRAY['초밥 세트','라멘','돈까스','우동','텐동']
      WHEN '치킨' THEN ARRAY['후라이드','양념치킨','간장치킨','마늘치킨','반반치킨']
      WHEN '양식' THEN ARRAY['스테이크','파스타','리조또','버거','샐러드']
      WHEN '중식' THEN ARRAY['짜장면','짬뽕','탕수육','마라탕','볶음밥']
      WHEN '피자' THEN ARRAY['페퍼로니','마르게리타','하와이안','불고기피자','콤비네이션']
      WHEN '분식' THEN ARRAY['떡볶이','순대','김밥','라볶이','튀김세트']
      ELSE ARRAY['메뉴A','메뉴B','메뉴C','메뉴D','메뉴E']
    END
  ) AS menu_name,
  unnest(
    CASE r.category
      WHEN '한식' THEN ARRAY[8000,9000,15000,9000,10000]
      WHEN '일식' THEN ARRAY[25000,12000,13000,9000,14000]
      WHEN '치킨' THEN ARRAY[18000,19000,19000,20000,19000]
      WHEN '양식' THEN ARRAY[25000,16000,17000,14000,12000]
      WHEN '중식' THEN ARRAY[7000,8000,18000,12000,8000]
      WHEN '피자' THEN ARRAY[20000,18000,19000,21000,22000]
      WHEN '분식' THEN ARRAY[5000,4000,3500,7000,6000]
      ELSE ARRAY[10000,10000,10000,10000,10000]
    END::numeric[]
  ) AS menu_price
) m;

-- ============================================================
-- 4. 라이더 30명
-- ============================================================
INSERT INTO tb_riders (name, phone, current_lat, current_lng, status, current_region, created_at, updated_at)
SELECT
  (ARRAY['김배달','이퀵','박라이더','최배송','정익스','강달려','조번개','윤스피드','장빠른','한신속',
         '임달릴','오스타트','신번쩍','권파워','송가자','류고고','배나나','홍삼이','문택배','양배달',
         '구오토','노바이크','남킥보드','천자전','하전기','전드론','서택시','안배송','유퀵킹','고달려'])[i],
  '010-' || LPAD((3000 + i * 11)::text, 4, '0') || '-' || LPAD((5000 + i * 17)::text, 4, '0'),
  (ARRAY[37.4979, 37.4837, 37.5145, 37.5572, 37.5247, 37.5735, 37.5509, 37.4783, 37.5633, 37.5326])[((i-1) % 10) + 1]
    + (random() * 0.01 - 0.005),
  (ARRAY[127.0276, 127.0066, 127.1058, 126.9236, 126.9019, 126.9769, 126.8496, 126.9516, 127.0371, 126.9648])[((i-1) % 10) + 1]
    + (random() * 0.01 - 0.005),
  (ARRAY['AVAILABLE','AVAILABLE','BUSY','AVAILABLE','BUSY','AVAILABLE','OFFLINE','AVAILABLE','BUSY','AVAILABLE'])[((i-1) % 10) + 1],
  (ARRAY['강남','서초','송파','마포','영등포','종로','강서','관악','성동','용산'])[((i-1) % 10) + 1],
  NOW() - INTERVAL '60 days' + (INTERVAL '1 day' * (i % 60)),
  NOW()
FROM generate_series(1, 30) AS i;

-- ============================================================
-- 5. 주문 10,000건
-- ============================================================
INSERT INTO tb_orders (
  customer_id, restaurant_id, rider_id, status, total_amount,
  delivery_address, delivery_lat, delivery_lng, region,
  order_time, accepted_time, prep_start_time, ready_time,
  pickup_time, delivered_time, cancelled_time, cancel_reason,
  estimated_delivery_minutes, actual_delivery_minutes,
  created_at, updated_at
)
SELECT
  -- customer: 1~200
  1 + abs(h(i)) % 200,

  -- restaurant: 지역에 맞는 음식점
  (SELECT id FROM tb_restaurants
   WHERE region = regions[((i-1) % 10) + 1] AND status = 'OPEN'
   ORDER BY abs(h(i + id::int))
   LIMIT 1),

  -- rider: 배달 관련 상태만 라이더 할당
  CASE WHEN status_val IN ('PICKED_UP','DELIVERING','DELIVERED')
    THEN 1 + abs(h(i * 7)) % 30
    ELSE NULL
  END,

  status_val,

  -- total_amount (임시, 나중에 업데이트)
  (15000 + abs(h(i * 3)) % 30000)::numeric(10,2),

  -- delivery_address
  regions[((i-1) % 10) + 1] || ' ' ||
  (ARRAY['대로','로','길'])[((i-1) % 3) + 1] || ' ' ||
  (abs(h(i)) % 200 + 1)::text || '번지',

  -- delivery_lat/lng
  lats[((i-1) % 10) + 1] + (sin(i::numeric) * 0.005),
  lngs[((i-1) % 10) + 1] + (cos(i::numeric) * 0.005),

  regions[((i-1) % 10) + 1],

  -- order_time
  base_time + (INTERVAL '1 second' * time_offset),

  -- accepted_time
  CASE WHEN status_val NOT IN ('PENDING','CANCELLED')
    THEN base_time + (INTERVAL '1 second' * time_offset) + (INTERVAL '1 minute' * (1 + abs(h(i*11)) % 5))
    ELSE NULL
  END,

  -- prep_start_time
  CASE WHEN status_val IN ('PREPARING','READY','PICKED_UP','DELIVERING','DELIVERED')
    THEN base_time + (INTERVAL '1 second' * time_offset) + (INTERVAL '1 minute' * (3 + abs(h(i*13)) % 5))
    ELSE NULL
  END,

  -- ready_time
  CASE WHEN status_val IN ('READY','PICKED_UP','DELIVERING','DELIVERED')
    THEN base_time + (INTERVAL '1 second' * time_offset) + (INTERVAL '1 minute' * (15 + abs(h(i*17)) % 15))
    ELSE NULL
  END,

  -- pickup_time
  CASE WHEN status_val IN ('PICKED_UP','DELIVERING','DELIVERED')
    THEN base_time + (INTERVAL '1 second' * time_offset) + (INTERVAL '1 minute' * (20 + abs(h(i*19)) % 10))
    ELSE NULL
  END,

  -- delivered_time
  CASE WHEN status_val = 'DELIVERED'
    THEN base_time + (INTERVAL '1 second' * time_offset) + (INTERVAL '1 minute' * (30 + abs(h(i*23)) % 20))
    ELSE NULL
  END,

  -- cancelled_time
  CASE WHEN status_val = 'CANCELLED'
    THEN base_time + (INTERVAL '1 second' * time_offset) + (INTERVAL '1 minute' * (2 + abs(h(i*29)) % 10))
    ELSE NULL
  END,

  -- cancel_reason
  CASE WHEN status_val = 'CANCELLED'
    THEN (ARRAY['고객 요청 취소','배달 지연','재료 소진','가게 사정','결제 실패'])[1 + abs(h(i*31)) % 5]
    ELSE NULL
  END,

  -- estimated_delivery_minutes
  CASE WHEN status_val NOT IN ('PENDING','CANCELLED')
    THEN 30 + abs(h(i*37)) % 20
    ELSE NULL
  END,

  -- actual_delivery_minutes
  CASE WHEN status_val = 'DELIVERED'
    THEN 25 + abs(h(i*41)) % 30
    ELSE NULL
  END,

  base_time + (INTERVAL '1 second' * time_offset),
  NOW()

FROM generate_series(1, 10000) AS i,
LATERAL (
  SELECT
    ARRAY['강남','서초','송파','마포','영등포','종로','강서','관악','성동','용산'] AS regions,
    ARRAY[37.4979, 37.4837, 37.5145, 37.5572, 37.5247, 37.5735, 37.5509, 37.4783, 37.5633, 37.5326] AS lats,
    ARRAY[127.0276, 127.0066, 127.1058, 126.9236, 126.9019, 126.9769, 126.8496, 126.9516, 127.0371, 126.9648] AS lngs,
    NOW() - INTERVAL '30 days' AS base_time,

    -- 시간 오프셋: 점심/저녁 피크
    (
      (abs(h(i)) % 30) * 86400
      +
      CASE
        WHEN abs(h(i * 5)) % 8 < 3 THEN 39600 + (abs(h(i * 43)) % 10800)  -- 11:00~14:00
        WHEN abs(h(i * 5)) % 8 < 6 THEN 61200 + (abs(h(i * 47)) % 14400)  -- 17:00~21:00
        ELSE 28800 + (abs(h(i * 53)) % 43200)                               -- 08:00~20:00
      END
    ) AS time_offset,

    -- 상태 분포: DELIVERED 70%, CANCELLED 10%, 나머지 20%
    CASE
      WHEN abs(h(i * 59)) % 100 < 70 THEN 'DELIVERED'
      WHEN abs(h(i * 59)) % 100 < 80 THEN 'CANCELLED'
      WHEN abs(h(i * 59)) % 100 < 83 THEN 'PENDING'
      WHEN abs(h(i * 59)) % 100 < 86 THEN 'ACCEPTED'
      WHEN abs(h(i * 59)) % 100 < 89 THEN 'PREPARING'
      WHEN abs(h(i * 59)) % 100 < 92 THEN 'READY'
      WHEN abs(h(i * 59)) % 100 < 95 THEN 'PICKED_UP'
      ELSE 'DELIVERING'
    END AS status_val
) params;

-- ============================================================
-- 6. 주문 항목 (주문당 2~3개)
-- ============================================================
INSERT INTO order_items (order_id, menu_item_name, quantity, unit_price, total_price)
SELECT
  o.id,
  menu_names[menu_idx],
  qty,
  menu_prices[menu_idx],
  menu_prices[menu_idx] * qty
FROM tb_orders o
CROSS JOIN LATERAL (
  SELECT CASE WHEN abs(h(o.id::int * 67)) % 4 = 0 THEN 3 ELSE 2 END AS item_count
) ic
CROSS JOIN LATERAL generate_series(1, ic.item_count) AS item_idx
CROSS JOIN LATERAL (
  SELECT
    ARRAY['된장찌개','김치찌개','불고기','비빔밥','제육볶음','초밥 세트','라멘','돈까스',
          '후라이드','양념치킨','간장치킨','파스타','리조또','버거','짜장면','짬뽕',
          '탕수육','마라탕','떡볶이','순대','김밥','페퍼로니','마르게리타','스테이크'] AS menu_names,
    ARRAY[8000,9000,15000,9000,10000,25000,12000,13000,
          18000,19000,19000,16000,17000,14000,7000,8000,
          18000,12000,5000,4000,3500,20000,18000,25000]::numeric[] AS menu_prices,
    1 + abs(h(o.id::int * (71 + item_idx))) % 24 AS menu_idx,
    1 + abs(h(o.id::int * (73 + item_idx))) % 3 AS qty
) menu_data;

-- 주문 total_amount를 실제 항목 합계로 업데이트
UPDATE tb_orders SET total_amount = sub.total
FROM (
  SELECT order_id, SUM(total_price) AS total
  FROM order_items
  GROUP BY order_id
) sub
WHERE tb_orders.id = sub.order_id;

-- ============================================================
-- 7. 결제 10,000건 (주문 1:1)
-- ============================================================
INSERT INTO tb_payments (order_id, amount, method, status, transaction_id, failure_reason, created_at, updated_at)
SELECT
  o.id,
  o.total_amount,
  CASE
    WHEN abs(h(o.id::int * 79)) % 100 < 60 THEN 'CARD'
    WHEN abs(h(o.id::int * 79)) % 100 < 85 THEN 'KAKAO_PAY'
    WHEN abs(h(o.id::int * 79)) % 100 < 95 THEN 'NAVER_PAY'
    ELSE 'CASH'
  END,
  CASE
    WHEN o.status = 'CANCELLED' THEN
      CASE WHEN abs(h(o.id::int * 97)) % 10 < 7 THEN 'REFUNDED' ELSE 'FAILED' END
    WHEN o.status = 'PENDING' THEN 'PENDING'
    ELSE 'COMPLETED'
  END,
  CASE WHEN o.status NOT IN ('PENDING')
    THEN 'TXN-' || LPAD(o.id::text, 8, '0') || '-' || SUBSTR(md5(o.id::text), 1, 8)
    ELSE NULL
  END,
  CASE WHEN o.status = 'CANCELLED' AND abs(h(o.id::int * 97)) % 10 >= 7
    THEN (ARRAY['잔액 부족','카드 한도 초과','결제 시간 초과','카드사 오류','네트워크 오류'])[1 + abs(h(o.id::int * 83)) % 5]
    ELSE NULL
  END,
  o.created_at,
  NOW()
FROM tb_orders o;

-- ============================================================
-- 8. 액션 로그 200건
-- ============================================================
INSERT INTO tb_action_logs (anomaly_type, region, action, result, action_result, executed_at, created_at)
SELECT
  (ARRAY['ORDER_DROP','DELIVERY_DELAY','CUSTOMER_CHURN','REJECTION_SPIKE'])[((i-1) % 4) + 1],
  (ARRAY['강남','서초','송파','마포','영등포','종로','강서','관악','성동','용산'])[((i-1) % 10) + 1],
  CASE ((i-1) % 4)
    WHEN 0 THEN '프로모션 쿠폰 발송 및 할인 이벤트 노출'
    WHEN 1 THEN '추가 라이더 배차 및 배달 경로 최적화'
    WHEN 2 THEN '이탈 위험 고객 대상 리텐션 쿠폰 발송'
    ELSE '음식점 운영 상태 확인 및 메뉴 점검 알림'
  END,
  CASE ((i-1) % 4)
    WHEN 0 THEN '쿠폰 ' || (10 + abs(h(i)) % 50) || '건 발송 완료'
    WHEN 1 THEN '라이더 ' || (1 + abs(h(i)) % 5) || '명 추가 배차'
    WHEN 2 THEN '리텐션 쿠폰 ' || (5 + abs(h(i)) % 20) || '건 발송'
    ELSE '음식점 ' || (1 + abs(h(i)) % 3) || '곳 점검 알림 전송'
  END,
  (ARRAY['SUCCESS','SUCCESS','SUCCESS','FAILED','PARTIAL'])[1 + abs(h(i * 89)) % 5],
  NOW() - INTERVAL '30 days' + (INTERVAL '1 hour' * (i * 3.6)),
  NOW() - INTERVAL '30 days' + (INTERVAL '1 hour' * (i * 3.6))
FROM generate_series(1, 200) AS i;

-- 헬퍼 함수 정리
DROP FUNCTION IF EXISTS h(int);

COMMIT;

-- 결과 확인
SELECT 'tb_customers' AS table_name, COUNT(*) AS row_count FROM tb_customers
UNION ALL SELECT 'tb_restaurants', COUNT(*) FROM tb_restaurants
UNION ALL SELECT 'tb_menus', COUNT(*) FROM tb_menus
UNION ALL SELECT 'tb_riders', COUNT(*) FROM tb_riders
UNION ALL SELECT 'tb_orders', COUNT(*) FROM tb_orders
UNION ALL SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL SELECT 'tb_payments', COUNT(*) FROM tb_payments
UNION ALL SELECT 'tb_action_logs', COUNT(*) FROM tb_action_logs
ORDER BY table_name;
