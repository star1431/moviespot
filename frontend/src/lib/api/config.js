function getEnvBase() {
  return process.env.REACT_APP_API_BASE_URL || '';
}

function getDefaultBase() {
  if (typeof window === 'undefined') return '';
  const host = window.location.hostname;
  if (host === 'localhost' || host === '127.0.0.1') return 'http://localhost:8080';
  return '';
}

function normalizeUrlBase(value) {
  return (value || '').replace(/\/+$/, '');
}

export function getBackendOrigin() {
  // OAuth 등 "/oauth2/..." 같이 "/api"가 없는 경로를 위해 backend origin을 제공
  const rawBase = getEnvBase() || getDefaultBase();
  if (!rawBase) {
    // 프로덕션 기본: 현재 오리진(nginx가 /oauth2, /api를 백엔드로 프록시)
    return typeof window !== 'undefined' ? window.location.origin : '';
  }
  return normalizeUrlBase(rawBase.replace(/\/api\/?$/, ''));
}

export function getApiBaseUrl() {
  const rawBase = getEnvBase() || getDefaultBase();
  const normalizedBase = normalizeUrlBase(rawBase);
  if (!normalizedBase) return '/api';
  return normalizedBase.endsWith('/api') ? normalizedBase : `${normalizedBase}/api`;
}

export const API_BASE_URL = getApiBaseUrl();


