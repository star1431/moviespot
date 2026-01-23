# TMDB API 연동 가이드


## 환경변수 설정

`.env.dev` 파일에 다음 환경변수

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

**설명**: 현재 상영 중인 영화 목록을 조회

**요청 URL**:
```
https://api.themoviedb.org/3/movie/now_playing?api_key={API_KEY}&page={page}&language=ko-KR
```

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `page` (선택): 페이지 번호 (기본값: 1)
- `language` (선택): 언어 코드 (기본값: ko-KR)

**응답 형식**: 인기 영화 목록과 동일


---

### 3. 영화 상세 정보 조회

**엔드포인트**: `GET /movie/{movie_id}`

**설명**: 특정 영화의 상세 정보를 조회

**요청 URL**:
```
https://api.themoviedb.org/3/movie/{movie_id}?api_key={API_KEY}&language=ko-KR
```

**경로 파라미터**:
- `movie_id` (필수): TMDB 영화 ID

**쿼리 파라미터**:
- `api_key` (필수): TMDB API 키
- `language` (선택): 언어 코드 (기본값: ko-KR)

**응답 예시**:
```json
{
  "id": 550,
  "title": "파이트 클럽",
  "release_date": "1999-10-15",
  "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
  "overview": "영화 설명...",
  "vote_average": 8.4,
  "genre_ids": [18, 53]
}
```

---

### 4. 영화 장르 목록 조회

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



