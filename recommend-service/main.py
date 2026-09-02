import logging
import py_eureka_client.eureka_client as eureka_client
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.config.settings import settings
from app.kafka.consumer import enrollment_consumer
from app.router import recommend_router

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """앱 시작/종료 시 실행되는 이벤트"""

    # 시작 시
    logger.info(f"[{settings.app_name}] 서비스 시작")

    # Eureka 등록
    try:
        await eureka_client.init_async(
            eureka_server=settings.eureka_server_url,
            app_name=settings.app_name,
            instance_port=settings.app_port,
            instance_host=settings.eureka_instance_host,
        )
        logger.info("[Eureka] 서비스 등록 완료")
    except Exception as e:
        logger.warning(f"[Eureka] 등록 실패 (개발 환경에서 무시 가능): {e}")

    # Kafka Consumer 시작
    try:
        enrollment_consumer.start()
        logger.info("[Kafka] Consumer 시작 완료")
    except Exception as e:
        logger.warning(f"[Kafka] Consumer 시작 실패: {e}")

    yield

    # 종료 시
    logger.info(f"[{settings.app_name}] 서비스 종료")
    enrollment_consumer.stop()
    await eureka_client.stop_async()


app = FastAPI(
    title="Recommend Service",
    description="온라인 강의 플랫폼 - 규칙 기반 강의 추천 서비스",
    version="0.0.1",
    lifespan=lifespan
)

# 라우터 등록
app.include_router(recommend_router.router)


@app.get("/health")
async def health():
    return {"status": "UP", "service": settings.app_name}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.app_port,
        reload=True
    )
