# MovieSpot - API 명세서 

## 기본 정보

- **Base URL**: `/api`
- **인증**: JWT 토큰 기반 (일부 엔드포인트는 인증 불필요)
- **응답 형식**: JSON
- **에러 응답 형식**: 
```json
{
  "error": "에러 메시지",
  "status": 400
}
```

> NOTE: 영화 목록 API(`/api/movies/**`)는 현재 `SliceResponseDto(content,page,size,hasNext)` 형태로 응답합니다.
> 리뷰 목록 API(`/api/reviews`)는 Spring `Slice` 직렬화 형태로 응답하며 `content`와 `hasNext`를 포함합니다.

---

## 1. 메인 화면 (DB 조회)

### 1.1 Top Rated 영화 목록 조회
**GET** `/movies/top-rated`

**설명**: Top Rated 영화 목록 조회 (DB에서 빠르게 조회, 10개씩, TMDB의 top_rated API 사용)

**인증**: 불필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**:
```json
{
  "content": [
    {
      "id": 550,
      "title": "파이트 클럽",
      "releaseDate": "1999-10-15",
      "posterUrl": "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
      "voteAverage": 8.4,
      "userAverageRating": 8.5
    }
  ],
  "page": 0,
  "size": 10,
  "hasNext": false
}
```

---

### 1.2 개봉 중인 영화 목록 조회
**GET** `/movies/now-playing`

**설명**: 개봉 중인 인기 영화 목록 조회 (DB에서 빠르게 조회, 10개씩, 한국 지역 기준)

**인증**: 불필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**: 인기 영화 목록과 동일 형식

---

## 2. 영화 목록 페이지 (TMDB API 직접 호출)

### 2.1 Top Rated 영화 목록 조회
**GET** `/movies/top-rated/list`

**설명**: Top Rated 영화 목록 조회 (TMDB API 직접 호출, 무제한 페이지네이션, TMDB의 top_rated API 사용)

**인증**: 불필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**: 인기 영화 목록과 동일 형식

---

### 2.2 인기 상영작 목록 조회
**GET** `/movies/now-playing/list`

**설명**: 개봉 중인 인기 영화 목록 조회 (TMDB API 직접 호출, 무제한 페이지네이션)

**인증**: 불필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**: 인기 영화 목록과 동일 형식

---

### 2.3 개봉 예정 영화 목록 조회
**GET** `/movies/upcoming/list`

**설명**: 개봉 예정 영화 목록 조회 (TMDB API 직접 호출, 무제한 페이지네이션)

**인증**: 불필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**: 인기 영화 목록과 동일 형식

---

### 2.4 영화 찾기 (필터링/정렬)
**GET** `/movies`

**설명**: 영화 찾기 (제목 검색, 장르 필터, 연도 범위, 정렬)

**인증**: 불필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기
- `keyword` (optional): 검색어
- `keywordType` (optional, default: `title`): `title` | `review`
  - `title`: 제목 기반(TMDB `search/movie` 또는 `discover/movie`)
  - `review`: 리뷰 키워드 기반(우리 DB `ReviewKeyword` 기반으로 영화 목록 조회)
- `genreId` (optional): 장르 ID 필터링 (예: `28` - 액션)
- `releaseYearFrom` (optional): 시작 연도 (예: `2020`)
- `releaseYearTo` (optional): 종료 연도 (예: `2024`)
- `sortBy` (optional, default: `latest`): 정렬 기준
  - `latest`: 최신순 (`release_date.desc`)
  - `rating`: 평점순 (`vote_average.desc`, 최소 투표 수 200명)

**동작 방식**:
- `keywordType=review` && `keyword` 존재: DB에서 리뷰 키워드로 영화 목록 조회
- 그 외:
  - `keyword`가 2글자 이상이면: TMDB `search` API 사용 (제목 검색)
  - 그 외: TMDB `discover` API 사용 (장르, 연도 범위, 정렬 필터링)

**Response 200**: 인기 영화 목록과 동일 형식

---

## 3. 영화 상세 정보

### 3.1 영화 상세 정보 조회
**GET** `/movies/{tmdbId}`

**설명**: 영화 상세 정보 조회 (DB 우선. DB에 없을 때만 TMDB 상세 호출 후 DB 저장)

**인증**: 선택사항 (로그인 시 사용자 개인 정보 포함)

**Path Parameters**:
- `tmdbId` (required): TMDB 영화 ID

**Query Parameters**:
- `commentPage` (optional, default: 0): 댓글 페이지 번호
- `commentSize` (optional, default: 10): 댓글 페이지 크기

**Response 200 (비로그인)**:
```json
{
  "tmdbId": 550,
  "title": "파이트 클럽",
  "releaseDate": "1999-10-15",
  "posterUrl": "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
  "overview": "영화 설명...",
  "voteAverage": 8.4,
  "voteCount": 25000,
  "runtime": 139,
  "genreIds": [18, 53],
  "userAverageRating": 8.5,
  "trailerUrl": "https://www.youtube.com/watch?v=abc123",
  "genres": [
    { "id": 28, "name": "액션" }
  ],
  "userRatings": {
    "content": [
      {
        "userId": 1,
        "nickname": "사용자1",
        "score": 9,
        "comment": "정말 좋은 영화입니다!",
        "isRecommended": true,
        "createdAt": "2026-01-26T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "hasNext": false
  },
  "myRatingCreatedAt": null,
  "myScore": null,
  "myComment": null,
  "myRecommended": null,
  "isWatched": false
}
```

**Response 200 (로그인)**:
```json
{
  "tmdbId": 550,
  "title": "파이트 클럽",
  "releaseDate": "1999-10-15",
  "posterUrl": "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
  "overview": "영화 설명...",
  "voteAverage": 8.4,
  "voteCount": 25000,
  "runtime": 139,
  "genreIds": [18, 53],
  "userAverageRating": 8.5,
  "trailerUrl": "https://www.youtube.com/watch?v=abc123",
  "userRatings": {
    "content": [
      {
        "userId": 1,
        "nickname": "사용자1",
        "score": 9,
        "comment": "정말 좋은 영화입니다!",
        "isRecommended": true,
        "createdAt": "2026-01-26T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "hasNext": false
  },
  "myRatingCreatedAt": "2026-01-26T10:00:00",
  "myScore": 9,
  "myComment": "정말 좋은 영화입니다!",
  "myRecommended": true,
  "isWatched": true
}
```

**주요 필드 설명**:
- `tmdbId`: TMDB 영화 ID
- `voteAverage`: TMDB 평균 평점
- `voteCount`: TMDB 투표 수
- `runtime`: 플레이타임 (분 단위)
- `genreIds`: 장르 ID 목록
- `userAverageRating`: 우리회원 평균 평점
- `trailerUrl`: 트레일러 YouTube URL (DB에 캐싱됨)
- `userRatings`: 우리회원 평점/코멘트 목록 (페이지네이션)
- `myRatingCreatedAt`, `myScore`, `myComment`, `myRecommended`: 내 평점/코멘트 정보 (로그인 시만)
- `isWatched`: 본 영화 여부 (로그인 시만)

---

## 4. 장르

### 4.1 장르 목록 조회
**GET** `/genres`

**설명**: 전체 장르 목록 조회 (DB에서 조회, 애플리케이션 시작 시 자동 초기화)

**인증**: 불필요

**Response 200**:
```json
[
  {
    "id": 28,
    "name": "액션"
  },
  {
    "id": 35,
    "name": "코미디"
  }
]
```

---

## 5. 본 영화 관리

### 5.1 본 영화 등록/수정
**POST** `/watched-movies`

**설명**: 본 영화 등록 또는 수정 (이미 등록된 경우 수정)

**인증**: 필요

**Request Body**:
```json
{
  "tmdbId": 550
}
```

**Response 200**: HTTP 200 OK (빈 바디)

---

### 5.2 본 영화 삭제
**DELETE** `/watched-movies/{tmdbId}`

**설명**: 본 영화 목록에서 삭제

**인증**: 필요

**Path Parameters**:
- `tmdbId` (required): TMDB 영화 ID

**Response 200**: HTTP 200 OK (빈 바디)

---

### 5.3 본 영화 목록 조회
**GET** `/watched-movies`

**설명**: 사용자가 본 영화 목록 조회

**인증**: 필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**:
```json
{
  "content": [
    {
      "tmdbId": 550,
      "title": "파이트 클럽",
      "posterUrl": "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
      "voteAverage": 8.4,
      "createdAt": "2026-01-26T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "hasNext": false
}
```

---

## 6. 사용자 평점/코멘트

### 6.1 평점/코멘트 등록/수정
**POST** `/ratings`

**설명**: 영화에 대한 사용자 평점 및 코멘트 등록 또는 수정

**인증**: 필요

**Request Body**:
```json
{
  "tmdbId": 550,
  "score": 9,
  "comment": "정말 좋은 영화입니다!",
  "isRecommended": true
}
```

**Response 200**: HTTP 200 OK (빈 바디)

---

### 6.2 평점/코멘트 삭제
**DELETE** `/ratings/{tmdbId}`

**설명**: 영화에 대한 사용자 평점 및 코멘트 삭제

**인증**: 필요

**Path Parameters**:
- `tmdbId` (required): TMDB 영화 ID

**Response 200**: HTTP 200 OK (빈 바디)

---

## 7. 리뷰 (참여형 커뮤니티)

### 7.1 리뷰 작성
**POST** `/reviews`

**설명**: 영화 리뷰 게시글 작성

**인증**: 필요

**Request Body**:
```json
{
  "tmdbId": 550,
  "title": "리뷰 제목",
  "content": "리뷰 내용",
  "score": 9,
  "keywords": ["데이트", "혼자", "힐링"]
}
```

**Response 201**:
```json
{
  "reviewId": 1,
  "title": "리뷰 제목",
  "content": "리뷰 내용",
  "score": 9,
  "viewCount": 0,
  "likeCount": 0,
  "createdAt": "2026-01-26T10:00:00"
}
```

---

### 7.2 리뷰 수정
**PUT** `/reviews/{reviewId}`

**설명**: 내가 작성한 리뷰 수정

**인증**: 필요

**Path Parameters**:
- `reviewId` (required): 리뷰 ID

**Request Body**:
```json
{
  "title": "수정된 리뷰 제목",
  "content": "수정된 리뷰 내용",
  "score": 8,
  "keywords": ["데이트", "슬픔"]
}
```

**Response 200**: 리뷰 작성 응답과 동일 형식

---

### 7.3 리뷰 삭제
**DELETE** `/reviews/{reviewId}`

**설명**: 내가 작성한 리뷰 삭제

**인증**: 필요

**Path Parameters**:
- `reviewId` (required): 리뷰 ID

**Response 200**: HTTP 200 OK (빈 바디)

---

### 7.4 리뷰 목록 조회
**GET** `/reviews`

**설명**: 리뷰 목록 조회 (최신순, 인기순, 좋아요순, 키워드 필터링, 영화별 필터링)

**인증**: 불필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기
- `sortBy` (optional): 정렬 기준 (`latest`, `popular`, `likes`)
- `keyword` (optional): 키워드 필터링 (예: "데이트", "혼자", "힐링", "슬픔")
- `movieTitle` (optional): 영화 제목 검색(부분 일치)
- `tmdbId` (optional): 영화별 리뷰 조회 (내부/특정 링크에서 사용 가능)

**Response 200**:
```json
{
  "content": [
    {
      "reviewId": 1,
      "title": "리뷰 제목",
      "content": "리뷰 내용...",
      "score": 9,
      "viewCount": 100,
      "likeCount": 50,
      "author": {
        "userId": 1,
        "nickname": "사용자1"
      },
      "movie": {
        "tmdbId": 550,
        "title": "파이트 클럽",
        "posterUrl": "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
      },
      "keywords": ["데이트", "혼자"],
      "createdAt": "2026-01-26T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "hasNext": true
}
```

---

### 7.5 리뷰 상세 조회
**GET** `/reviews/{reviewId}`

**설명**: 리뷰 상세 정보 조회 (조회수 증가)

**인증**: 선택사항 (로그인 시 좋아요 여부 포함)

**Path Parameters**:
- `reviewId` (required): 리뷰 ID

**Response 200**:
```json
{
  "reviewId": 1,
  "title": "리뷰 제목",
  "content": "리뷰 내용",
  "score": 9,
  "viewCount": 101,
  "likeCount": 50,
  "isLiked": false,
  "author": {
    "userId": 1,
    "nickname": "사용자1"
  },
  "movie": {
    "tmdbId": 550,
    "title": "파이트 클럽",
    "posterUrl": "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
  },
  "keywords": ["데이트", "혼자"],
  "createdAt": "2026-01-26T10:00:00",
  "updatedAt": "2026-01-26T10:00:00"
}
```

---

### 7.6 리뷰 좋아요/취소
**POST** `/reviews/{reviewId}/like`

**설명**: 리뷰 좋아요 또는 취소 (토글)

**인증**: 필요

**Path Parameters**:
- `reviewId` (required): 리뷰 ID

**Response 200**:
```json
{
  "isLiked": true,
  "likeCount": 51
}
```

---

## 8. 개인 정보 기록 (나의 컬렉션)

### 8.1 관심 장르 목록 조회
**GET** `/users/me/genres`

**설명**: 내가 설정한 관심 장르 목록 조회

**인증**: 필요

**Response 200**:
```json
[
  {
    "genreId": 28,
    "name": "액션"
  },
  {
    "genreId": 35,
    "name": "코미디"
  }
]
```

---

### 8.2 관심 장르 등록
**POST** `/users/me/genres`

**설명**: 관심 장르 등록

**인증**: 필요

**Request Body**:
```json
{
  "genreId": 28
}
```

**Response 200**: HTTP 200 OK (빈 바디)

---

### 8.3 관심 장르 삭제
**DELETE** `/users/me/genres/{genreId}`

**설명**: 관심 장르 삭제

**인증**: 필요

**Path Parameters**:
- `genreId` (required): 장르 ID

**Response 200**: HTTP 200 OK (빈 바디)

---

### 8.4 관심 키워드 목록 조회
**GET** `/users/me/keywords`

**설명**: 내가 설정한 관심 키워드 목록 조회

**인증**: 필요

**Response 200**:
```json
[
  "데이트",
  "혼자",
  "힐링"
]
```

---

### 8.5 관심 키워드 등록
**POST** `/users/me/keywords`

**설명**: 관심 키워드 등록

**인증**: 필요

**Request Body**:
```json
{
  "keywordName": "데이트"
}
```

**Response 200**: HTTP 200 OK (빈 바디)

---

### 8.6 관심 키워드 삭제
**DELETE** `/users/me/keywords`

**설명**: 관심 키워드 삭제

**인증**: 필요

**Request Body**:
```json
{
  "keywordName": "데이트"
}
```

**Response 200**: HTTP 200 OK (빈 바디)

---

### 8.7 내가 작성한 리뷰 목록 조회
**GET** `/users/me/reviews`

**설명**: 내가 작성한 리뷰 목록 조회

**인증**: 필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**: 리뷰 목록 조회와 동일 형식

---

### 8.8 내가 본 영화 목록 조회
**GET** `/users/me/watched-movies`

**설명**: 내가 본 영화 목록 관리 (5.3과 동일, 인증된 사용자 본인만 조회)

**인증**: 필요

**Query Parameters**:
- `page` (optional, default: 0): 페이지 번호
- `size` (optional, default: 10): 페이지 크기

**Response 200**: 본 영화 목록 조회와 동일 형식

---

## 9. 사용자

### 9.1 사용자 프로필 조회
**GET** `/users/me`

**설명**: 현재 로그인한 사용자 프로필 조회

**인증**: 필요

**Response 200**:
```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "사용자1",
  "provider": "google",
  "createdAt": "2026-01-26T10:00:00"
}
```

---

### 9.2 사용자 프로필 수정
**PUT** `/users/me`

**설명**: 사용자 프로필 수정 (닉네임 등)

**인증**: 필요

**Request Body**:
```json
{
  "nickname": "새로운 닉네임"
}
```

**Response 200**: 사용자 프로필 조회와 동일 형식

---

## 에러 코드

- **400 Bad Request**: 잘못된 요청
- **401 Unauthorized**: 인증 필요
- **403 Forbidden**: 권한 없음
- **404 Not Found**: 리소스를 찾을 수 없음
- **500 Internal Server Error**: 서버 오류
