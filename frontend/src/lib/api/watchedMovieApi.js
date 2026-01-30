import { apiClient } from './client';

export const watchedMovieAPI = {
  createOrUpdate: (data) => apiClient.post('/watched-movies', data),
  delete: (tmdbId) => apiClient.delete(`/watched-movies/${tmdbId}`),
  getList: (page = 0, size = 10) =>
    apiClient.get('/watched-movies', { params: { page, size } }),
};


