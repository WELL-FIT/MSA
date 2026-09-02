import httpx
import logging
from app.config.settings import settings
from app.model.schemas import EnrollmentHistoryResponse

logger = logging.getLogger(__name__)


class EnrollmentServiceClient:
    """
    Enrollment Service REST 클라이언트
    - 수강 이력 조회 (ACTIVE 강의 ID 목록)
    """

    def __init__(self):
        self.base_url = settings.enrollment_service_url

    async def get_enrollment_history(self, user_id: int) -> EnrollmentHistoryResponse:
        """
        GET /enrollments/internal/history/{userId}
        사용자의 수강 중인 강의 ID 목록 조회
        """
        url = f"{self.base_url}/api/enrollments/internal/history/{user_id}"
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url)
                response.raise_for_status()
                data = response.json()
                return EnrollmentHistoryResponse(**data)
        except httpx.HTTPError as e:
            logger.error(f"[EnrollmentClient] 수강 이력 조회 실패 - userId: {user_id}, error: {e}")
            # 실패 시 빈 이력 반환 (추천 서비스는 비핵심 기능)
            return EnrollmentHistoryResponse(userId=user_id, activeCourseIds=[])


enrollment_client = EnrollmentServiceClient()
