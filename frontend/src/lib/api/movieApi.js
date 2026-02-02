import { apiClient } from './client';

export const movieAPI = {
  getTopRated: (page = 0, size = 10) =>
    apiClient.get('/movies/top-rated', { params: { page, size } }),
  getNowPlaying: (page = 0, size = 10) =>
    apiClient.get('/movies/now-playing', { params: { page, size } }),

  getTopRatedList: (page = 0, size = 10) =>
    apiClient.get('/movies/top-rated/list', { params: { page, size } }),
  getNowPlayingList: (page = 0, size = 10) =>
    apiClient.get('/movies/now-playing/list', { params: { page, size } }),
  getUpcomingList: (page = 0, size = 10) =>
    apiClient.get('/movies/upcoming/list', { params: { page, size } }),
  searchMovies: (params) => apiClient.get('/movies', { params }),

  getMovieDetail: (tmdbId, commentPage = 0, commentSize = 10) =>
    apiClient.get(`/movies/${tmdbId}`, { params: { commentPage, commentSize } }),
};


