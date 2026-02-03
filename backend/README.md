# MovieSpot Backend

MovieSpot 프로젝트의 Spring Boot 백엔드 서버입니다.

## 기술 스택

- **Java** 21
- **Spring Boot** 4.1.0 (Web / WebFlux)
- **Spring Security** — 인증 및 인가
- **Spring OAuth2 Client** — 소셜 로그인 (Google, Naver, Kakao)
- **Spring Data JPA** — 데이터베이스 연동
- **MySQL** 8.0 — 데이터베이스
- **JJWT** 0.12.6 — JWT 발급/검증

## 실행 방법

### 로컬 실행

Windows (PowerShell/CMD):

```bash
# Gradle 빌드
.\\gradlew build

# 애플리케이션 실행
.\\gradlew bootRun
```

macOS/Linux:

```bash
# Gradle 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

기본 포트는 `8080` 입니다.

### Docker 실행

`backend/Dockerfile`을 사용해 컨테이너 이미지를 빌드/실행합니다.

```bash
# Docker 이미지 빌드
docker build -t moviespot-backend .

# 컨테이너 실행 (필요 환경변수와 함께)
docker run -p 8080:8080 \
	-e SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/moviespot?useSSL=false&serverTimezone=UTC" \
	-e SPRING_DATASOURCE_USERNAME=your_user \
	-e SPRING_DATASOURCE_PASSWORD=your_pass \
	-e JWT_SECRET=change_me \
	-e JWT_ACCESS_TOKEN_EXP_MS=3600000 \
	-e JWT_REFRESH_TOKEN_EXP_MS=1209600000 \
	-e CORS_ALLOWED_ORIGINS=http://localhost:3000 \
	-e GOOGLE_API_ID=... -e GOOGLE_API_SECRET=... \
	-e NAVER_API_ID=... -e NAVER_API_SECRET=... \
	-e KAKAO_API_ID=... -e KAKAO_API_SECRET=... \
	moviespot-backend
```

프로젝트 루트의 Compose 파일로 프론트/백엔드(+Nginx)를 함께 올릴 수 있습니다:

```bash
docker compose -f ../docker-compose.dev.yml up -d --build
```

## 환경 변수

`src/main/resources/application.yml`에서 다음 값을 참조합니다.

- **데이터베이스**
	- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- **JWT**
	- `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXP_MS`, `JWT_REFRESH_TOKEN_EXP_MS`
- **CORS**
	- `CORS_ALLOWED_ORIGINS`
- **OAuth2 Client**
	- `GOOGLE_API_ID`, `GOOGLE_API_SECRET`
	- `NAVER_API_ID`, `NAVER_API_SECRET`
	- `KAKAO_API_ID`, `KAKAO_API_SECRET`

## 주요 기능

- **사용자 인증/인가 (JWT, OAuth2)**: 소셜 로그인 성공 시 액세스/리프레시 토큰 발급
- **영화 도메인**: 목록/검색/상세, TMDB 연동 캐시
- **리뷰/평점**: 작성/수정/삭제, 좋아요 토글, 정렬 제공
- **장르/컬렉션/시청 관리**: 관심 장르/키워드, 본 영화 기록
- **프로필/온보딩**: 사용자 정보 및 초기 선호도 설정

## API 문서

상세 API 명세는 프로젝트 루트 문서 폴더를 참고하세요:
- [docs/05_api_spec.md](../docs/05_api_spec.md)

백엔드 전체 설계/플로우는 다음 문서에서 확인할 수 있습니다:
- [docs/02_project_design.md](../docs/02_project_design.md)

