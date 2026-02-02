import axios from 'axios';
import { API_BASE_URL } from './config';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

let refreshPromise = null;
const refreshAccessToken = async () => {
  if (!refreshPromise) {
    refreshPromise = apiClient
      .post('/auth/refresh')
      .then(() => true)
      .catch(() => false)
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
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

    const status = error.response?.status;
    const originalRequest = error.config;

    if (status === 401 && originalRequest && !originalRequest._retry) {
      const url = originalRequest.url || '';
      const isAuthEndpoint =
        url.includes('/auth/login') ||
        url.includes('/auth/signup') ||
        url.includes('/auth/logout') ||
        url.includes('/auth/refresh');

      if (!isAuthEndpoint) {
        originalRequest._retry = true;
        const refreshed = await refreshAccessToken();
        if (refreshed) {
          return apiClient(originalRequest);
        }
      }
    }

    return Promise.reject(error);
  }
);


