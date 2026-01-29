// 이미지 URL 처리
export const getImageUrl = (path, size = 'w500') => {
  if (!path) return '/placeholder-movie.jpg';
  if (path.startsWith('http')) return path;
  return `https://image.tmdb.org/t/p/${size}${path}`;
};

// 날짜 포맷팅
export const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

// 평점 포맷팅
export const formatRating = (rating) => {
  if (!rating && rating !== 0) return 'N/A';
  return rating.toFixed(1);
};

// 페이지네이션 헬퍼
export const getPaginationInfo = (page, size, hasNext) => {
  return {
    currentPage: page,
    pageSize: size,
    hasNext,
  };
};

// 로컬 스토리지 헬퍼
export const storage = {
  get: (key) => {
    if (typeof window === 'undefined') return null;
    try {
      return localStorage.getItem(key);
    } catch (e) {
      return null;
    }
  },
  set: (key, value) => {
    if (typeof window === 'undefined') return;
    try {
      localStorage.setItem(key, value);
    } catch (e) {
      console.error('Storage set error:', e);
    }
  },
  remove: (key) => {
    if (typeof window === 'undefined') return;
    try {
      localStorage.removeItem(key);
    } catch (e) {
      console.error('Storage remove error:', e);
    }
  },
};

// 인증 헬퍼
export const isAuthenticated = () => {
  return !!storage.get('token');
};

