# 사내복지 관리 플랫폼

집행된 복지 예산과 실제 이용 로그를 하나로 묶어, 복지 프로그램 운영을
추측이 아닌 데이터로 판단하게 만드는 사내복지 관리 플랫폼입니다.

기존 MSA 백엔드를 수정하지 않는다는 제약 아래, 도메인 치환·비동기 상태 관리·
집계 기반 AI 처방을 프론트엔드와 분석 서비스 레이어에서 구현했습니다.

## 📌 기획 배경

기업의 복지 담당자는 아래와 같은 문제를 겪습니다.

- 복지 프로그램별 신청·이용 기록이 흩어져 있어 취합에 손이 많이 간다
- 어떤 프로그램에 예산이 묶여 있는지 파악하기 어렵다
- 프로그램 등록부터 승인까지의 처리가 수동으로 이루어진다

이 프로젝트는 복지 프로그램 등록 → 임직원 신청 → 승인 → **이용 로그 집계 → AI 예산 재배치 제안**으로 이어지는 흐름을, 서비스 단위로 분리된 MSA 구조로 구현했습니다.


## 💡 비즈니스 가치

### 해결하는 것

복지 운영의 문제는 개별 불편이 아니라 하나의 사슬 구조로 이어져 있습니다.

정보 파편화 → 수동 운영 → ROI 측정 부재 → 추측 기반 재탐색
(기록이 흩어짐) (정산·취합이 (이용률과 예산 (성과가 나빠도 근거가
사람 손) 효율을 못 봄) 없어 다시 추측으로)
↑ │
└──────────────────────────────────────────────────────┘


기존 대안들은 모두 **신청·거래에서 멈추고 분석으로 이어지지 않습니다.**

| 대안 | 강점 | 멈추는 지점 |
|---|---|---|
| 엑셀·수기 | 유연하고 비용 없음 | 취합 자체가 업무가 됨 |
| 복지몰·포인트 | 카탈로그와 결제 처리 | 거래에서 종료. 데이터가 내부에 안 남음 |
| 개별 제휴 계약 | 단가 협상 가능 | 공급사 수만큼 정산 업무가 선형 증가 |
| 그룹웨어 내장 | 이미 설치되어 있음 | 신청 접수까지만 지원 |

이 플랫폼은 신청 로그가 자동으로 쌓이고 그것이 곧 분석 입력이 되는 구조로,
사슬의 연결 고리인 **이용 기록의 부재**를 끊습니다.

### 이해관계자별 가치

| 대상 | 얻는 것 |
|---|---|
| **HR 담당자** | 이용 현황 취합·월별 정산·보고서 작성에 드는 반복 업무 축소. 프로그램 개폐 판단의 근거 확보 |
| **경영진** | 예산 증액·유지 결정에 필요한 정량 지표 확보. 복지 예산이 실제로 쓰이는지 확인 가능 |
| **임직원** | 실제 수요가 반영된 프로그램 구성. 이용률 낮은 항목에 묶인 예산이 필요한 곳으로 이동 |
| **공급사** | 이용 데이터 기반의 계약 갱신 근거 |

### 측정 지표

| 지표 | 산식 | 의미 |
|---|---|---|
| 복지 예산 실이용률 | Σ `enrollment_count` / 임직원 수 | 배정 예산이 실제로 쓰이는 비율 |
| 저이용 항목 예산 비중 | 이용률 20% 미만 항목의 `price` 합 / 총 `price` 합 | 묶여 있는 예산의 규모 |
| AI 제안 채택률 | 채택 건수 / 제안 건수 | 처방이 실제 의사결정에 쓰이는지 |
| 프로그램별 집행 효율 | `price` × `enrollment_count` | 단가 대비 도달 인원 |

앞의 두 지표는 현재 스키마에서 추가 수집 없이 바로 계산됩니다.

### 확장 가능성

도메인 특화 로직이 얇고, 상태 전이와 집계 구조가 서비스로 분리되어 있어
"신청 → 승인 → 이용 로그 → 예산 분석" 형태를 갖는 다른 업무에 그대로
적용됩니다. 교육비 지원, 사내 자산 대여, 부서 예산 집행 관리 등이 해당합니다.
템플릿 도메인을 복지로 치환한 과정 자체가 이 이식성을 검증한 사례입니다.



| 테이블 | 도메인 해석 |
|---|---|
| `users` (role: `INSTRUCTOR`) | 복지 프로그램 공급업체 / 관리자 |
| `courses` | 사내 복지 프로그램 |
| `enrollments` | 복지 이용 신청 |
| `payments` | 신청 승인 후 포인트 차감 |
| `courses.enrollment_count` | 이용 임직원 수 |# 사내복지 관리 플랫폼

## 📌 기획 배경

기업의 복지 담당자는 아래와 같은 문제를 겪습니다.

- 복지 프로그램별 신청·이용 기록이 흩어져 있어 취합의 복잡함
- 프로그램의 ROI 파악의 어려움
- 프로그램의 CRUD 의사결정 근거가 부족
- 프로그램 등록부터 승인까지의 수동 처리

이 프로젝트는 복지 프로그램 등록 → 임직원 신청 → 승인 → **이용 로그 집계 → AI 예산 재배치 제안**으로 이어지는 흐름을, 서비스 단위로 분리된 MSA 구조로 구현합니다.

백엔드 테이블·필드·상태값은 원본 구조를 유지하고, 화면 해석만 사내복지 도메인으로 사용합니다.

| 테이블 | 도메인 해석 |
|---|---|
| `users` (role: `INSTRUCTOR`) | 복지 프로그램 공급업체 / 관리자 |
| `courses` | 사내 복지 프로그램 |
| `enrollments` | 복지 이용 신청 |
| `payments` | 신청 승인 후 포인트 차감 |
| `courses.enrollment_count` | 이용 임직원 수 |

---

## 🧠 시스템 아키텍처

```
[Client · Vue :3000]
        │
        ▼
┌───────────────────────────────────────────────┐
│            API Gateway  :8080                 │
│      단일 진입점 · 라우팅 · JWT 검증            │
└───────────────────────────────────────────────┘
        │
        ├──▶ user-service        :8081   users
        ├──▶ course-service      :8082   courses
        ├──▶ enrollment-service  :8083   enrollments
        ├──▶ payment-service     :8084   payments
        └──▶ recommend-service   :8085   집계 · AI (읽기 전용)

  Eureka :8761      Auth Server :9000
  Kafka  :9092      MariaDB     :3379
```

`depends_on` 헬스체크에 따라 아래 순서로 기동됩니다.

```
MariaDB / Kafka → Eureka → Auth Server → API Gateway + 4개 서비스 → Recommend
```

서비스 간 호출은 IP가 아니라 Eureka에 등록된 서비스 이름으로 이루어지며, 상태 전이는 Kafka 이벤트로 전달됩니다.

### 신청 → 이용 로그 → 분석 흐름

```
① 이용 로그 축적                    [구현 완료]
   복지 신청 → 예산 집행 승인 → ACTIVE 전이 → enrollment.completed
                                        │
                                        ▼
② 집계와 AI 처방                    [Sprint 2]
   ┌─────────────────────────┐   ┌─────────────────────────┐
   │ 집계 API                 │──▶│ AI 처방 API              │
   │ 프로그램별 집행액·이용률   │   │ 집계 결과를 LLM에 전달    │
   │ 저이용 판별 (20% 미만)    │   │ 예산 재배치 우선순위 생성  │
   └─────────────────────────┘   └─────────────────────────┘
                                        │
                                        ▼
③ HR 대시보드                       [Sprint 2]
   예산 vs 이용률 차트 · 단가 vs 이용자 산점도 · AI 진단 카드
```

**설계 의도** — AI는 원본 로그를 직접 읽지 않습니다. 저이용 판별은 규칙이 처리하고, LLM은 집계된 수치를 받아 재배치 제안 문장만 생성합니다. 판별과 생성을 분리해 결과의 재현성을 확보하고, LLM 호출이 실패해도 집계 결과만으로 대시보드가 동작하도록 fallback 경로를 둡니다.

### 이벤트 흐름

| 토픽 | 발행 | 구독 |
|---|---|---|
| `payment.completed` | payment-service | enrollment-service |
| `enrollment.completed` | enrollment-service | recommend-service |

신청 요청은 `PENDING`으로 먼저 저장된 뒤, `payment.completed` 수신 시 `ACTIVE`로 전이됩니다. 사용자는 별도 조작 없이 상태가 갱신됩니다.

---

## 🧩 서비스 구성

| 서비스 | 스택 | 포트 | 소유 테이블 | 역할 |
|---|---|---|---|---|
| eureka-server | Spring Boot | 8761 | — | 서비스 등록·탐색 |
| auth-server | Spring Boot | 9000 | `users` (읽기) | OAuth2 토큰 발급, JWK 공개 |
| api-gateway | Spring Cloud Gateway | 8080 | — | 단일 진입점, 라우팅, JWT 검증 |
| user-service | Spring Boot | 8081 | `users` | 회원가입, 사용자 조회 |
| course-service | Spring Boot | 8082 | `courses` | 프로그램 등록·조회 |
| enrollment-service | Spring Boot | 8083 | `enrollments` | 신청, 상태 관리 |
| payment-service | Spring Boot | 8084 | `payments` | 승인 처리 |
| recommend-service | FastAPI | 8085 | 읽기 전용 | 이용 이력 집계, 추천 |

`auth-server`와 `api-gateway`는 소스가 포함되어 있지 않으며, 배포된 이미지를 사용합니다.

---

## ⚙️ 기술 스택

- **Backend**: Spring Boot, Spring Cloud Gateway, Spring Cloud Netflix Eureka
- **Auth**: OAuth2 Authorization Code, JWT (JWK 검증)
- **Messaging**: Kafka (KRaft 모드, `confluentinc/cp-kafka:7.7.0`)
- **Analytics / AI**: FastAPI, py-eureka-client, Kafka consumer
- **DB**: MariaDB 11.2 (테이블 단위 분리)
- **Frontend**: Vue 3, Vite, Pinia, Vue Router, axios
- **API Docs**: springdoc-openapi (Spring), FastAPI 내장 docs
- **실행 환경**: Docker Compose

---

## 📂 프로젝트 구조

```
.
├── docker-compose.yml           # 사전 빌드 이미지 실행 (ARM64)
├── docker-compose.build.yml     # 소스 빌드 실행
├── init-db/
│   ├── 01_init.sql              # 스키마 DDL
│   └── 02_seed.sql              # 복지 도메인 시드 데이터
├── eureka-server/
├── user-service/
├── course-service/
├── enrollment-service/
├── payment-service/
├── recommend-service/           # FastAPI · 집계 및 추천
│   ├── main.py
│   └── app/
│       ├── router/              # API 엔드포인트
│       ├── service/             # 추천 로직
│       ├── client/              # enrollment · course 조회
│       ├── kafka/               # enrollment.completed 구독
│       └── config/              # 설정 · 토큰 검증
└── vue-frontend/                # Vue 3 + Vite
    └── src/                     # views · components · api · store · router
```

`.env`, `*.tar`, `node_modules/`, Gradle 빌드 산출물은 `.gitignore` 대상입니다.

---

## 🚀 실행 방법

### 1. 사전 요구사항

- Docker / Docker Compose
- Node.js 20 이상

### 2. 이미지 로드

`auth-server`, `api-gateway`는 배포된 이미지 아카이브가 필요합니다.

```bash
docker load -i infra-images.tar
docker images    # msa-lecture/auth-server:1.0 태그 확인
```

### 3. 백엔드 기동

소스에서 빌드하는 경우:

```bash
docker compose -f docker-compose.build.yml build --no-cache
docker compose -f docker-compose.build.yml up -d
```

사전 빌드된 이미지로 실행하는 경우 (ARM64):

```bash
docker compose up -d --pull never
```

### 4. 상태 확인

```bash
docker compose logs -f                     # 전체 로그
docker compose logs -f recommend-service   # 개별 서비스 로그
```

Eureka 대시보드에서 서비스 등록 상태를 확인합니다 — http://localhost:8761

### 5. 프론트엔드 실행

`vue-frontend/.env` 파일을 직접 생성합니다.

```
VITE_API_BASE_URL=http://localhost:8080
VITE_AUTH_SERVER_URL=http://localhost:8080
VITE_CLIENT_ID=your_client_id
VITE_CLIENT_SECRET=your_client_secret
VITE_REDIRECT_URI=http://localhost:3000/callback
```

```bash
cd vue-frontend
npm install
npm run dev
```

http://localhost:3000 으로 접속합니다.
`/api`, `/oauth2`, `/login`, `/logout`, `/userinfo` 요청은 Vite 프록시를 통해 게이트웨이(8080)로 전달됩니다.

### 6. 종료

```bash
docker compose down       # 컨테이너 정리
docker compose down -v    # 볼륨까지 삭제 (DB 초기화)
```

---

## 🔑 테스트 계정

`init-db/02_seed.sql`에서 로드됩니다.

| 이메일 | 비밀번호 | role |
|---|---|---|
| `employee@company.example` | `password` | `STUDENT` |
| `admin@company.example` | `password` | `INSTRUCTOR` |

복지 프로그램 시드 20건이 함께 등록되며, 카테고리와 `enrollment_count`에 편차를 두어 저이용 항목 판별을 검증할 수 있게 구성했습니다.

---

## 📡 API

인증은 OAuth2 Authorization Code 방식으로, 게이트웨이를 거쳐 auth-server에서 토큰을 발급받습니다.

| Method | Endpoint | 서비스 |
|---|---|---|
| POST | `/oauth2/token` | auth-server |
| POST | `/api/users/register` | user |
| GET | `/api/users/me` | user |
| GET | `/api/users/{id}` | user |
| POST | `/api/courses` | course |
| GET | `/api/courses` | course |
| GET | `/api/courses/{id}` | course |
| GET | `/api/courses/category/{category}` | course |
| POST | `/api/enrollments` | enrollment |
| GET | `/api/enrollments/my` | enrollment |
| GET | `/api/enrollments/user/{userId}` | enrollment |
| GET | `/api/payments/{id}` | payment |
| GET | `/api/payments/user/{userId}` | payment |
| GET | `/api/recommend/{user_id}` | recommend |

`internal` 경로(`/api/courses/internal/**`, `/api/payments/internal/request` 등)는 서비스 간 호출 전용입니다.

### Swagger UI

```
http://localhost:8081/swagger-ui.html   # user
http://localhost:8082/swagger-ui.html   # course
http://localhost:8083/swagger-ui.html   # enrollment
http://localhost:8084/swagger-ui.html   # payment
http://localhost:8085/docs              # recommend (FastAPI)
```

---

## 🗄 데이터 모델

| 테이블 | 주요 컬럼 | 상태값 |
|---|---|---|
| `users` | email, password, name, role | `STUDENT` \| `INSTRUCTOR` |
| `courses` | title, category, price, instructor_id, enrollment_count | `ACTIVE` \| `INACTIVE` |
| `enrollments` | user_id, course_id | `PENDING` \| `ACTIVE` \| `CANCELLED` |
| `payments` | user_id, course_id, amount, transaction_id | `PENDING` \| `COMPLETED` \| `FAILED` \| `CANCELLED` |

`courses.category` — `HEALTH`, `SELF_DEVELOPMENT`, `LEISURE`, `PSYCHOLOGICAL`, `FAMILY`, `FINANCIAL`, `CULTURE`, `OTHER`

`enrollments`는 `(user_id, course_id)` 유니크 제약으로 중복 신청을 차단합니다.

집계에 사용되는 값은 `courses.price`(단가), `courses.enrollment_count`(이용 임직원 수), `enrollments`의 상태별 건수입니다.

---


## 👥 팀원

| 이름 | 담당 |
|---|---|
| 방윤지 | PO |
| 박수빈 | Scrum Master |
| 이송미 | 대시보드 |
| 이찬혁 | Kafka, 프론트 |
| 박기람 | 프론트 |
| 정민교 | 프론트 |



