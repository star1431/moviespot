const RAW_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

export const API_BASE_URL = RAW_BASE.endsWith('/api') ? RAW_BASE : `${RAW_BASE}/api`;


