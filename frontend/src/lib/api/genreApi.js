import { apiClient } from './client';

export const genreAPI = {
  getGenres: () => apiClient.get('/genres'),
};


