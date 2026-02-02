import { apiClient } from './client';

export const ratingAPI = {
  createOrUpdate: (data) => apiClient.post('/ratings', data),
  delete: (tmdbId) => apiClient.delete(`/ratings/${tmdbId}`),
};


