# MovieSpot - 개발 환경/실행가이드

## 1. .env.dev 작성

* `.env.dev.example` 참고하여 프로젝트 최상위 루트에서 `.env.dev` 작성
* 각 항목당 빈값인 경우 채워서 사용

```bash
# db 설정
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=moviespotdb
MYSQL_USER=
MYSQL_PASSWORD=

# backend 설정
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/moviespotdb?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect

SPRING_DEVTOOLS_RESTART_ENABLED=true
SPRING_DEVTOOLS_RESTART_EXCLUDE=static/**,public/**,META-INF/maven/**,META-INF/resources/**,resources/**,build/**,.gradle/**

CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost

SPRING_PROFILES_ACTIVE=dev

JWT_SECRET=
JWT_ACCESS_TOKEN_EXP_MS=1800000
JWT_REFRESH_TOKEN_EXP_MS=604800000

# oauth
GOOGLE_API_ID=
GOOGLE_API_SECRET=
NAVER_API_ID=
NAVER_API_SECRET=
KAKAO_API_ID=
KAKAO_API_SECRET=

# tmdb api 연동
TMDB_API_KEY=
TMDB_API_BASE_URL=https://api.themoviedb.org/3 # 무비정보 베이스
TMDB_API_IMAGE_BASE_URL=https://image.tmdb.org/t/p/w500 # 포스트이미지 w200, w500, w700


# frontend 설정
REACT_APP_API_BASE_URL=http://localhost:8080
```

---

## 2. docker compose 실행

* 프로젝트 최상위 루트에서 실행

```bash
# docker compose dev 환경 빌드
docker compose -f docker-compose.dev.yml build --no-cache

# docker compose dev 환경 실행
docker compose -f docker-compose.dev.yml up -d

# 실행 확인
docker compose -f docker-compose.dev.yml ps
```

* 이후 브라우저에서 `http://localhost` 접속하여 확인


---

## 3. MYSQL만 별도 실행

> MYSQL만 별도 실행하고, backend, frontend 각각 수동 실행 시

### 3.1 MYSQL 컨테이너 독립 실행

```bash
# 기존 컨테이너 정지
docker compose -f docker-compose.dev.yml stop

# MYSQL 컨테이너 실행
docker compose -f docker-compose.dev.yml start db
```

### 3.2 backend 실행

* IDE에서 backend 프로젝트 열기
* 실행하기전 : IDE에서 `backend` 프로젝트 열고,
* `.env.dev` 파일을 `.env.local` 로 복사 및 아래 내용 참고하여 URL 수정

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/moviespotdb?.....
# 위의 URL을 아래로 변경
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/moviespotdb?......
```

* 이후 아래 명령어 실행 or IDE에서 실행

```bash
./gradlew bootRun
```

### 3.3 frontend 실행

* 최상위 루트에서 `frontend` 디렉토리로 이동
* `.env.local` 작성

```bash
REACT_APP_API_BASE_URL=http://localhost:8080
```

* 이후 아래 명령어 실행

```bash
# 최상위 루트에서
cd frontend
# 최초 실행시 필요한 패키지 설치
npm install
# 개발서버 실행
npm start
```

* 이후 브라우저에서 `http://localhost:3000` 접속하여 확인