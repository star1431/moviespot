# MovieSpot - TMDB API 연동 가이드 정리

## 환경변수 설정

환경변수 예시 (로컬/도커 환경에 맞게 설정)

```bash
# TMDB API 키 (필수)
TMDB_API_KEY=your_api_key_here

# TMDB API 기본 URL (선택사항, 기본값 사용 가능)
TMDB_API_BASE_URL=https://api.themoviedb.org/3

# TMDB 이미지 기본 URL (선택사항, 기본값 사용 가능)
TMDB_API_IMAGE_BASE_URL=https://image.tmdb.org/t/p/w500
```

## API 엔드포인트 확인

### 1. 인기 영화 목록 조회

**엔드포인트**: `GET /movie/popular`

**설명**: 현재 인기 있는 영화 목록을 조회

**요청 URL**:
```
https://api.themoviedb.org/3/movie/popular?api_key={API_KEY}&page={page}&language=ko-KR
```

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `page` (선택): 페이지 번호 (기본값: 1)
- `language` (선택): 언어 코드 (기본값: ko-KR)

**응답 예시**:
```json
{
  "page": 1,
  "results": [
    {
      "id": 550,
      "title": "파이트 클럽",
      "release_date": "1999-10-15",
      "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
      "overview": "영화 설명...",
      "vote_average": 8.4,
      "vote_count": 25000,
      "runtime": 139,
      "genre_ids": [18, 53]
    }
  ],
  "total_pages": 500,
  "total_results": 10000
}
```

---

### 2. 개봉 중인 영화 목록 조회

**엔드포인트**: `GET /movie/now_playing`

**설명**: 현재 상영 중인 영화 목록을 조회 (한국 지역 기준)

**요청 URL**:
```
https://api.themoviedb.org/3/movie/now_playing?api_key={API_KEY}&page={page}&language=ko-KR&region=KR
```

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `page` (선택): 페이지 번호 (기본값: 1)
- `language` (선택): 언어 코드 (기본값: ko-KR)
- `region` (선택): 지역 코드 (기본값: KR)

**응답 형식**: 인기 영화 목록과 동일

---

### 3. 개봉 예정 영화 목록 조회

**엔드포인트**: `GET /movie/upcoming`

**설명**: 개봉 예정 영화 목록을 조회 (한국 지역 기준)

**요청 URL**:
```
https://api.themoviedb.org/3/movie/upcoming?api_key={API_KEY}&page={page}&language=ko-KR&region=KR
```

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `page` (선택): 페이지 번호 (기본값: 1)
- `language` (선택): 언어 코드 (기본값: ko-KR)
- `region` (선택): 지역 코드 (기본값: KR)

**응답 형식**: 인기 영화 목록과 동일

---

### 4. 영화 상세 정보 조회

**엔드포인트**: `GET /movie/{movie_id}`

**설명**: 특정 영화의 상세 정보를 조회 (한국어 우선, 없으면 영어 fallback)

**요청 URL**:
```
https://api.themoviedb.org/3/movie/{movie_id}?api_key={API_KEY}&language=ko-KR
```

**경로 파라미터**:
- `movie_id` (필수): TMDB 영화 ID

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `language` (선택): 언어 코드 (기본값: ko-KR)

**언어 Fallback 로직**:
- 먼저 한국어(`ko-KR`)로 요청
- `overview`가 비어있거나 null이면 영어(`en-US`) 버전으로 재요청
- 영어 버전의 `overview`와 `title`(한국어 제목이 비어있을 경우)을 사용

**응답 예시**:
```json
{
  "id": 550,
  "title": "파이트 클럽",
  "release_date": "1999-10-15",
  "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
  "overview": "영화 설명...",
  "vote_average": 8.4,
  "vote_count": 25000,
  "runtime": 139,
  "genre_ids": [18, 53]
}
```

**주요 필드 설명**:
- `runtime`: 영화 플레이타임 (분 단위)
- `vote_count`: TMDB 사용자 투표 수
- `genre_ids`: 장르 ID 목록

---

### 5. 영화 영상(트레일러/티저) 목록 조회

**엔드포인트**: `GET /movie/{movie_id}/videos`

**설명**: 영화의 트레일러, 티저 등 영상 목록 조회 (한국어 우선, 없으면 영어 fallback)

**요청 URL**:
```
https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key={API_KEY}&language=ko-KR
```

**경로 파라미터**:
- `movie_id` (필수): TMDB 영화 ID

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `language` (선택): 언어 코드 (기본값: ko-KR)

**언어 Fallback 로직**:
- 먼저 한국어(`ko-KR`)로 요청
- 한국어 영상이 없거나 비어있으면 영어(`en-US`) 버전으로 재요청

**응답 예시**:
```json
{
  "results": [
    {
      "key": "abc123",
      "site": "YouTube",
      "type": "Trailer",
      "official": true
    }
  ]
}
```

---

### 6. 영화 장르 목록 조회

**엔드포인트**: `GET /genre/movie/list`

**설명**: 사용 가능한 모든 영화 장르 목록 조회

**요청 URL**:
```
https://api.themoviedb.org/3/genre/movie/list?api_key={API_KEY}&language=ko-KR
```

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `language` (선택): 언어 코드 (기본값: ko-KR)

**응답 예시**:
```json
{
  "genres": [
    {
      "id": 28,
      "name": "액션"
    },
    {
      "id": 12,
      "name": "모험"
    },
    {
      "id": 16,
      "name": "애니메이션"
    }
  ]
}
```

---

### 7. 영화 검색

**엔드포인트**: `GET /search/movie`

**설명**: 영화 제목으로 검색 (2글자 이상)

**요청 URL**:
```
https://api.themoviedb.org/3/search/movie?api_key={API_KEY}&query={query}&page={page}&language=ko-KR
```

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `query` (필수): 검색어 (2글자 이상)
- `page` (선택): 페이지 번호 (기본값: 1)
- `language` (선택): 언어 코드 (기본값: ko-KR)

**응답 형식**: 인기 영화 목록과 동일

---

### 8. 영화 찾기 (Discover)

**엔드포인트**: `GET /discover/movie`

**설명**: 다양한 필터 조건으로 영화를 검색하고 발견

**요청 URL**:
```
https://api.themoviedb.org/3/discover/movie?api_key={API_KEY}&page={page}&language=ko-KR&sort_by={sort_by}
```

**주요 쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `page` (선택): 페이지 번호 (기본값: 1)
- `language` (선택): 언어 코드 (기본값: ko-KR)
- `sort_by` (선택): 정렬 기준
  - `release_date.desc`: 최신순 (기본값)
  - `vote_average.desc`: 평점순
- `with_genres`: 장르 ID 필터 (예: `28` - 액션)
- `primary_release_date.gte`: 개봉일 시작 범위 (예: `2020-01-01`)
- `primary_release_date.lte`: 개봉일 종료 범위 (예: `2024-12-31`)
- `vote_count.gte`: 최소 투표 수 (평점순일 때 기본값: 200)
- `vote_average.gte`: 최소 평점
- `with_runtime.gte`: 최소 플레이타임 (분 단위)
- `with_runtime.lte`: 최대 플레이타임 (분 단위)
- `include_adult`: 성인 콘텐츠 포함 여부 (기본값: false)

**정렬 옵션**:
- `latest`: `release_date.desc` (최신순)
- `rating`: `vote_average.desc` (평점순, `vote_count.gte=200` 자동 적용)

**필터링 예시**:
```
/discover/movie?api_key={API_KEY}&sort_by=release_date.desc&with_genres=28&primary_release_date.gte=2020-01-01&primary_release_date.lte=2024-12-31&include_adult=false
```

---

## 이미지 URL 구성

* TMDB API는 포스터 이미지 경로만 반환

### 포스터 이미지 URL 형식

```
{TMDB_API_IMAGE_BASE_URL}{poster_path}
```

**예시**:
- `poster_path`: `/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg`
- `TMDB_API_IMAGE_BASE_URL`: `https://image.tmdb.org/t/p/w500`
- **결과**: `https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg`

### 이미지 크기 옵션

| 크기 코드 | 너비 | 설명 |
|---------|------|------|
| `w92` | 92px | 작은 썸네일 |
| `w154` | 154px | 작은 포스터 |
| `w185` | 185px | 중간 포스터 |
| `w342` | 342px | 큰 포스터 |
| `w500` | 500px | 매우 큰 포스터 (기본값) |
| `w780` | 780px | 초대형 포스터 |
| `original` | 원본 | 원본 크기 |

**현재 설정**: `w500` (500px 너비)

---

## 참고 자료

- [TMDB API 공식 문서](https://developers.themoviedb.org/3)
- [TMDB API 인증 가이드](https://developers.themoviedb.org/3/getting-started/authentication)
- [TMDB 이미지 API 가이드](https://developers.themoviedb.org/3/getting-started/images)
