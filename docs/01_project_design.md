# 프로젝트 설계

## 1. moviespot 서비스 설명

* TMDB(The Movie Database) API를 활용한 영화 정보 서비스
* 사용자는 영화를 검색하고, 평점을 남기며, 리뷰를 작성

## 2. 전체 흐름

### 2.1 메인 페이지

메인 페이지에서는 다음 영화 목록을 **DB에서 빠르게 조회**합니다 (각 10개씩):
- **현재 상영 중**: `GET /api/movies/now-playing` (DB 조회)
- **요즘 인기 영화**: `GET /api/movies/popular` (DB 조회)

**특징**:
- DB에 저장된 최신 데이터를 빠르게 조회
- 각 카테고리당 10개씩만 저장하여 빠른 응답 속도
- 하루에 한 번 자동 업데이트 (오늘 자정 기준)

### 2.2 영화 목록 페이지

영화 목록 페이지에서는 다음 영화 목록을 **TMDB API에서 직접 조회** (Slice):
- **현재 상영 중**: `GET /api/movies/now-playing/list` (TMDB API 직접 호출)
- **개봉 예정**: `GET /api/movies/upcoming/list` (TMDB API 직접 호출)
- **요즘 인기 영화**: `GET /api/movies/popular/list` (TMDB API 직접 호출)
- **영화 찾기**: `GET /api/movies` (TMDB API 직접 호출)
  - 제목 검색 (`keyword`)
  - 장르 필터 (`genreId`)
  - 연도 범위 필터 (`releaseYearFrom`, `releaseYearTo`)
  - 정렬 (`sortBy`: `latest`, `rating`)

**특징**:
- 실시간 TMDB API 데이터 조회
- 무제한 페이지네이션 지원
- 다양한 필터링 및 정렬 옵션 제공

### 2.3 영화 상세 페이지

영화 상세 정보는 **TMDB API에서 직접 조회**:
- `GET /api/movies/{tmdbId}` (TMDB API 직접 호출)
- 로그인 여부에 따라 사용자 개인 정보 포함 여부 결정
  - 비로그인: 기본 영화 정보 + 우리회원 평균 평점
  - 로그인: 기본 영화 정보 + 우리회원 평균 평점 + 내 평점/코멘트 + 본 영화 여부

**특징**:
- 한국어 우선, 없으면 영어 fallback
- 트레일러 URL은 DB에 캐싱하여 재사용
- 우리회원 평균 평점 자동 계산

## 3. 데이터 저장 전략

### 3.1 DB 저장 대상

다음 데이터는 DB에 저장:
- **메인 페이지용 영화 목록**: `popular`, `now_playing` 각 10개씩
- **장르 목록**: 애플리케이션 시작 시 자동 삽입 (tmdb 장르 -> db)
- **사용자 평점/코멘트**: 사용자가 작성한 평점 및 코멘트
- **본 영화 목록**: 사용자가 본 영화 기록
- **리뷰**: 사용자가 작성한 리뷰 게시글

**코멘트, 리뷰, 본영화 등록시 해당 영화정보를 db 저장하는 방식**

### 3.2 DB 미저장 대상

다음 데이터는 DB에 저장하지 않고 TMDB API에서 직접 조회:
- **영화 목록 페이지용 영화 목록**: `popular/list`, `now-playing/list`, `upcoming/list`
- **영화 찾기 결과**: `GET /api/movies` 검색/필터링 결과
- **영화 상세 정보**: `GET /api/movies/{tmdbId}` 상세 정보

**이유**:
- 실시간 데이터 제공
- DB 저장 공간 절약
- 데이터 신선도 보장

## 4. API 구조

### 4.1 메인 페이지용 API (DB 조회)

- `GET /api/movies/popular`: 전체 인기작 목록 (DB 조회, 10개)
- `GET /api/movies/now-playing`: 인기 상영작 목록 (DB 조회, 10개)

### 4.2 영화 목록 페이지용 API (TMDB API 직접 호출)

- `GET /api/movies/popular/list`: 전체 인기작 목록 (TMDB API)
- `GET /api/movies/now-playing/list`: 인기 상영작 목록 (TMDB API)
- `GET /api/movies/upcoming/list`: 개봉 예정 영화 목록 (TMDB API)
- `GET /api/movies`: 영화 찾기 (제목, 장르, 연도, 정렬 필터링)

### 4.3 영화 상세 API

- `GET /api/movies/{tmdbId}`: 영화 상세 정보 조회 (TMDB API, 로그인 여부에 따라 개인 정보 포함)

## 5. 무비 언어 전략

### 5.1 영화 상세 정보

1. 먼저 한국어(`ko-KR`)로 요청
2. `overview`가 비어있거나 null이면 영어(`en-US`) 버전으로 재요청
3. 영어 버전의 `overview`와 `title`(한국어 제목이 비어있을 경우)을 사용

### 5.2 영화 영상(트레일러)

1. 먼저 한국어(`ko-KR`)로 요청
2. 한국어 영상이 없거나 비어있으면 영어(`en-US`) 버전으로 재요청

## 6. 성능 최적화

### 6.1 메인 페이지

- DB에 10개씩만 저장하여 빠른 조회
- 하루에 한 번 자동 업데이트로 최신성 유지

### 6.2 영화 목록 페이지

- TMDB API 직접 호출로 실시간 데이터 제공
- 페이지네이션으로 필요한 만큼만 조회

### 6.3 영화 상세 페이지

- 트레일러 URL은 DB에서 재사용
- 비로그인 사용자는 불필요한 값 null
