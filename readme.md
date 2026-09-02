# The following practice code is intended for educational purposes only. For contact :  audit@korea.ac.kr, Sungryel Lim Ph.D

# This practice code is not a completed commercial version but has been developed for educational purposes; supplementation is required depending on the deployment objective for use as a commercial service.

# 전체 백엔드 기동 순서 (depends_on 기반)
MariaDB / Kafka (인프라)
  → Eureka (서비스 등록)
    → Auth Server (인증)
      → API Gateway + 4개 서비스
        → Recommend Service

# 공통 이미지 파일 로드 (API Gateway, Auth Server)
docker load -i infra-images.tar

# msa-lecture/auth-server:1.0 등 태그 확인
docker images

## 프로젝트 루트에서 (초기 트러블슈팅/리빌드 고려, 캐시 없이 빌드, 컨테이너는 묶어서 백그라운드로 실행)
docker compose build --no-cache
docker compose up -d

## 또는 한줄로
docker compose build --no-cache && docker compose up -d

# 로그 확인
## 전체 로그 한번에 보기
docker compose logs -f

## 또는 개별 컨테이너 로그 보기
docker compose logs -f [서비스명]

docker compose logs -f mariadb
docker compose logs -f kafka
docker compose logs -f eureka-server
docker compose logs -f auth-server
docker compose logs -f api-gateway
docker compose logs -f user-service
docker compose logs -f course-service
docker compose logs -f enrollment-service
docker compose logs -f payment-service
docker compose logs -f recommend-service

# 전체 종료 (또는 컨테이너 빌드 중, 실패 시에 기존 컨테이너 정리)
docker compose down

# 서버 기동 상태 확인
http://localhost:8761/

# 프론트엔드 실행
## 로컬 실행 방법
cd vue-frontend
npm install
npm run dev

## 브라우저에서 접속
http://localhost:3000 
