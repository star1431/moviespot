import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키 자동 전송
});

// 요청 인터셉터: 쿠키는 withCredentials로 자동 전송됨
// localStorage에 토큰이 있으면 Authorization 헤더에 추가 (하위 호환성)
apiClient.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터: 에러 처리
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Network Error 상세 로깅
    if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
      console.error('Network Error:', {
        message: error.message,
        code: error.code,
        config: {
          url: error.config?.url,
          baseURL: error.config?.baseURL,
          method: error.config?.method,
        },
      });
    }
    
    if (error.response?.status === 401) {
      // 인증 실패 시 localStorage 토큰 제거 (하위 호환성)
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
        // 로그인/로그아웃 페이지가 아닌 경우에만 리다이렉트
        const pathname = window.location.pathname;
        if (!pathname.startsWith('/login') && !pathname.startsWith('/signup') && pathname !== '/') {
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

// 영화 API
export const movieAPI = {
  // 메인 페이지용 (DB 조회)
  getTopRated: (page = 0, size = 10) =>
    apiClient.get('/movies/top-rated', { params: { page, size } }),
  getNowPlaying: (page = 0, size = 10) =>
    apiClient.get('/movies/now-playing', { params: { page, size } }),

  // 영화 목록 페이지용 (TMDB API)
  getTopRatedList: (page = 0, size = 10) =>
    apiClient.get('/movies/top-rated/list', { params: { page, size } }),
  getNowPlayingList: (page = 0, size = 10) =>
    apiClient.get('/movies/now-playing/list', { params: { page, size } }),
  getUpcomingList: (page = 0, size = 10) =>
    apiClient.get('/movies/upcoming/list', { params: { page, size } }),
  searchMovies: (params) => apiClient.get('/movies', { params }),

  // 영화 상세
  getMovieDetail: (tmdbId, commentPage = 0, commentSize = 10) =>
    apiClient.get(`/movies/${tmdbId}`, {
      params: { commentPage, commentSize },
    }),
};

// 장르 API
export const genreAPI = {
  getGenres: () => apiClient.get('/genres'),
};

// 본 영화 API
export const watchedMovieAPI = {
  createOrUpdate: (data) => apiClient.post('/watched-movies', data),
  delete: (tmdbId) => apiClient.delete(`/watched-movies/${tmdbId}`),
  getList: (page = 0, size = 10) =>
    apiClient.get('/watched-movies', { params: { page, size } }),
};

// 평점/코멘트 API
export const ratingAPI = {
  createOrUpdate: (data) => apiClient.post('/ratings', data),
  delete: (tmdbId) => apiClient.delete(`/ratings/${tmdbId}`),
};

// 리뷰 API
export const reviewAPI = {
  create: (data) => apiClient.post('/reviews', data),
  update: (reviewId, data) => apiClient.put(`/reviews/${reviewId}`, data),
  delete: (reviewId) => apiClient.delete(`/reviews/${reviewId}`),
  getList: (params) => apiClient.get('/reviews', { params }),
  getDetail: (reviewId) => apiClient.get(`/reviews/${reviewId}`),
  toggleLike: (reviewId) => apiClient.post(`/reviews/${reviewId}/like`),
};

// 사용자 API
export const userAPI = {
  getProfile: () => apiClient.get('/users/me'),
  updateProfile: (data) => apiClient.put('/users/me', data),
  getGenres: () => apiClient.get('/users/me/genres'),
  addGenre: (data) => apiClient.post('/users/me/genres', data),
  deleteGenre: (genreId) => apiClient.delete(`/users/me/genres/${genreId}`),
  getKeywords: () => apiClient.get('/users/me/keywords'),
  addKeyword: (data) => apiClient.post('/users/me/keywords', data),
  deleteKeyword: (data) => apiClient.delete('/users/me/keywords', { data }),
  getReviews: (page = 0, size = 10) =>
    apiClient.get('/users/me/reviews', { params: { page, size } }),
  getWatchedMovies: (page = 0, size = 10) =>
    apiClient.get('/users/me/watched-movies', { params: { page, size } }),
};

export { apiClient };
export default apiClient;

