import { apiClient } from './client';

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


