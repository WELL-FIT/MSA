-- 사내복지 관리 도메인 시드 데이터
-- 기존 테이블/필드/상태값은 유지하고, 화면 해석만 복지 도메인으로 사용함
-- 로그인 테스트 계정 비밀번호: password

INSERT IGNORE INTO users
    (id, email, password, name, role, created_at, updated_at)
VALUES
    (1, 'employee@company.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '김직원', 'USER', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (2, 'welfare.vendor@company.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '웰니스파트너', 'ADMIN', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

-- instructor_id=2는 복지 공급업체 사용자
-- enrollment_count는 해당 복지 프로그램을 이용한 임직원 수로 해석함
INSERT IGNORE INTO courses
    (id, title, description, category, price, instructor_id, enrollment_count, status, created_at, updated_at)
VALUES
    (1, '종합 건강검진 패키지', '기본 검진과 선택 검사를 포함한 임직원 건강검진 프로그램입니다.', 'HEALTH', 180000.00, 2, 0, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (2, '심리상담 1회권', '전문 상담사와 함께하는 비대면 또는 대면 심리상담 이용권입니다.', 'PSYCHOLOGICAL', 50000.00, 2, 0, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (3, '마음건강 코칭 4주', '스트레스와 번아웃 관리를 위한 4주 코칭 프로그램입니다.', 'PSYCHOLOGICAL', 120000.00, 2, 0, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (4, '헬스장 이용권', '제휴 체육시설을 자유롭게 이용할 수 있는 월 이용권입니다.', 'HEALTH', 60000.00, 2, 12, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (5, '필라테스 그룹 수업', '기초 체력과 자세 교정을 위한 필라테스 그룹 프로그램입니다.', 'HEALTH', 90000.00, 2, 8, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (6, '요가·명상 클래스', '업무 전후 긴장 완화와 집중력 향상을 돕는 웰니스 클래스입니다.', 'HEALTH', 70000.00, 2, 5, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (7, '임직원 종합 치과검진', '구강검진과 예방 진료를 지원하는 제휴 치과 프로그램입니다.', 'HEALTH', 80000.00, 2, 0, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (8, '독감 예방접종 지원', '지정 병원에서 사용할 수 있는 계절성 예방접종 지원 프로그램입니다.', 'HEALTH', 30000.00, 2, 18, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (9, '영양 상담 프로그램', '개인 건강 목표에 맞춘 식습관 분석과 영양 상담을 제공합니다.', 'HEALTH', 45000.00, 2, 3, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (10, '가족 건강검진 지원', '임직원 가족을 대상으로 제공하는 건강검진 지원 프로그램입니다.', 'FAMILY', 150000.00, 2, 0, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (11, '도서 구매 포인트', '업무와 자기계발에 필요한 도서를 구매할 수 있는 포인트입니다.', 'SELF_DEVELOPMENT', 50000.00, 2, 15, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (12, '온라인 외국어 학습권', '영어와 중국어 등 온라인 외국어 학습 콘텐츠 이용권입니다.', 'SELF_DEVELOPMENT', 75000.00, 2, 10, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (13, '직무 역량 컨설팅', '개인별 직무 목표와 성장 방향을 설계하는 1:1 컨설팅입니다.', 'SELF_DEVELOPMENT', 110000.00, 2, 2, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (14, '재택근무 장비 지원', '재택근무 환경 개선을 위한 모니터와 주변기기 지원 프로그램입니다.', 'OTHER', 200000.00, 2, 6, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (15, '문화생활 관람권', '영화, 공연, 전시 관람에 사용할 수 있는 문화 포인트입니다.', 'CULTURE', 50000.00, 2, 9, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (16, '반려동물 케어 지원', '반려동물 병원과 돌봄 서비스 이용을 지원하는 복지 프로그램입니다.', 'FAMILY', 100000.00, 2, 0, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (17, '경조사 지원 서비스', '임직원 경조사 발생 시 축하·위로 지원을 신청할 수 있습니다.', 'FAMILY', 100000.00, 2, 1, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (18, '자녀 교육 상담', '자녀 학습과 진로에 대한 전문 상담을 제공하는 프로그램입니다.', 'FAMILY', 80000.00, 2, 0, 'INACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (19, '휴양시설 숙박 지원', '제휴 휴양시설 예약에 사용할 수 있는 숙박 지원 프로그램입니다.', 'LEISURE', 130000.00, 2, 4, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (20, '생활법률 상담', '일상생활에서 발생하는 법률 문제를 상담할 수 있는 서비스입니다.', 'FINANCIAL', 60000.00, 2, 0, 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
