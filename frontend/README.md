# MovieSpot Frontend

MovieSpot 프로젝트의 Next.js 프론트엔드 애플리케이션입니다.

## 기술 스택

- **Next.js** 16.1.4
- **React** 19.2.3
- **Axios** 1.13.4 — HTTP 클라이언트
- **Tailwind CSS** 4 — 스타일링
- **Lucide React** 0.563.0 — 아이콘
- **Swiper** 12.1.0 — 캐러셀 컴포넌트

이미지 로딩은 Next Image 최적화를 사용하며 `image.tmdb.org` 원격 이미지를 허용하도록 설정되어 있습니다.

## 실행 방법

### 로컬 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

개발 서버는 http://localhost:3000 에서 실행됩니다.

### 프로덕션 빌드

```bash
# 프로덕션 빌드
npm run build

# 프로덕션 서버 실행
npm start
```

### Docker 실행 (단일 프론트엔드 컨테이너)

```bash
# Docker 이미지 빌드
docker build -t moviespot-frontend .

# 컨테이너 실행 (API 주소는 환경변수로 설정)
docker run -p 3000:3000 \
	-e REACT_APP_API_BASE_URL=http://localhost:8080 \
	moviespot-frontend
```

### Docker Compose (프로젝트 루트)

프로젝트 루트의 Compose 파일을 사용해 프론트/백엔드(+Nginx)를 함께 올릴 수 있습니다.

```bash
# 개발용
docker compose -f docker-compose.dev.yml up -d --build

# 배포용
docker compose -f docker-compose.prod.yml up -d --build
```

> 상세 네트워킹/프록시는 루트의 `nginx.conf` 및 각 Compose 파일을 참고하세요.

## 환경 변수

- **`REACT_APP_API_BASE_URL`**: 백엔드 API 기본 주소 (예: `http://localhost:8080`)
	- 클라이언트에서 `/api` 경로가 자동으로 붙도록 구성되어 있습니다. 예를 들어 `REACT_APP_API_BASE_URL=http://localhost:8080` 이면 실제 요청은 `http://localhost:8080/api/...` 로 전송됩니다.
	- Next.js 특성상 빌드/실행 환경에서 값을 주입해야 하며, Docker/배포 환경에서는 컨테이너(혹은 오케스트레이터)에서 환경변수를 설정하세요.

## 프로젝트 구조

```
frontend/
├── Dockerfile
├── next.config.mjs          # Next 이미지 원격 도메인 허용, standalone 출력
├── package.json
├── public/
│   └── font/
└── src/
		├── app/                 # App Router 페이지
		│   ├── collection/
		│   ├── login/
		│   ├── movies/
		│   ├── oauth2/
		│   ├── onboarding/
		│   ├── profile/
		│   ├── reviews/
		│   ├── signup/
		│   ├── globals.css
		│   ├── layout.js
		│   └── page.js
		├── assets/
		│   └── css/
		├── components/
		│   ├── layout/
		│   ├── movies/
		│   ├── providers/
		│   └── ui/
		├── hooks/
		│   └── useAuth.js
		└── lib/
				├── api.js
				├── utils.js
				└── api/             # 도메인별 API 모듈 (client/config 포함)
```

## 주요 기능

- **소셜 로그인 (OAuth2 + JWT)**: 로그인/회원가입/로그아웃 흐름 및 토큰 갱신 처리
- **영화 목록/검색/상세**: TMDB 연동, 이미지 최적화, 트레일러/세부 정보 표시
- **리뷰 관리**: 작성/수정/삭제, 좋아요 토글, 최신/인기 정렬
- **나의 컬렉션/시청 관리**: 본 영화 기록, 관심 장르/키워드 설정
- **사용자 프로필 및 온보딩**: 초기 선호도 설정과 마이페이지 제공
- **반응형 UI & 공통 컴포넌트**: Tailwind 기반 스타일과 공용 UI 컴포넌트

## API 통신

- `src/lib/api/client.js` 에서 **Axios 인스턴스**를 구성합니다 (`withCredentials: true`).
- 401 응답 시 `/auth/refresh` 로 **액세스 토큰 자동 갱신**을 시도한 뒤 원 요청을 재시도합니다.
- 기본 주소는 `src/lib/api/config.js` 의 `REACT_APP_API_BASE_URL` 을 사용하며 실제 요청은 `/api` 경로를 접두로 합니다.

## 이미지 로딩 설정

- `next.config.mjs` 에서 **TMDB 이미지 도메인**(`image.tmdb.org/t/p/**`)을 허용해 Next Image 최적화를 적용합니다.

## 개발 가이드

- **Lint**: `npm run lint`
- **문서**: 루트 `docs/` 폴더의 API 명세/배포 가이드를 참고하세요.
	- API 명세: [docs/05_api_spec.md](../docs/05_api_spec.md)
	- 배포: [docs/06_deploy.md](../docs/06_deploy.md)
- **백엔드**: 스펙/엔드포인트는 [backend/](../backend/) 및 문서에서 확인하세요.

## 참고

- 프로젝트 공통 정보와 전체 스택은 루트 문서인 [README.md](../README.md) 를 참고하세요
