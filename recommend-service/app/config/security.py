import httpx
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import jwt, JWTError
from app.config.settings import settings

security = HTTPBearer()

_jwks_cache: dict = {}


async def get_jwks() -> dict:
    global _jwks_cache
    if not _jwks_cache:
        async with httpx.AsyncClient() as client:
            response = await client.get(settings.jwk_set_uri)
            response.raise_for_status()
            _jwks_cache = response.json()
    return _jwks_cache


def get_signing_key(token: str, jwks: dict) -> dict:
    unverified_header = jwt.get_unverified_header(token)
    kid = unverified_header.get("kid")

    if not kid:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="토큰 헤더에 kid가 없습니다",
            headers={"WWW-Authenticate": "Bearer"},
        )

    for key in jwks.get("keys", []):
        if key.get("kid") == kid:
            return key

    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="일치하는 공개키를 찾을 수 없습니다",
        headers={"WWW-Authenticate": "Bearer"},
    )


async def verify_token(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> dict:
    token = credentials.credentials

    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="유효하지 않은 토큰입니다",
        headers={"WWW-Authenticate": "Bearer"},
    )

    try:
        jwks = await get_jwks()
        signing_key = get_signing_key(token, jwks)

        payload = jwt.decode(
            token,
            signing_key,
            algorithms=["RS256"],
            issuer=settings.jwt_issuer_uri,
            options={"verify_aud": False}
        )
        return payload

    except JWTError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"유효하지 않은 토큰입니다: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )


async def verify_service_token(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> dict:
    payload = await verify_token(credentials)
    scopes = payload.get("scope", "").split()

    if "service.read" not in scopes:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="서비스 권한이 없습니다"
        )

    return payload