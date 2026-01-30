import { apiClient } from './client';

export const reviewAPI = {
  create: (data) => apiClient.post('/reviews', data),
  update: (reviewId, data) => apiClient.put(`/reviews/${reviewId}`, data),
  delete: (reviewId) => apiClient.delete(`/reviews/${reviewId}`),
  getList: (params) => apiClient.get('/reviews', { params }),
  getDetail: (reviewId) => apiClient.get(`/reviews/${reviewId}`),
  toggleLike: (reviewId) => apiClient.post(`/reviews/${reviewId}/like`),
};


