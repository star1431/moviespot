'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { apiClient, userAPI } from '@/lib/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const isLoggingOutRef = useRef(false);
  const isRefreshingRef = useRef(false);

  const fetchUser = useCallback(async () => {
    if (isLoggingOutRef.current) return false;

    try {
      const response = await userAPI.getProfile();
      if (!response || response.status !== 200) {
        setUser(null);
        return false;
      }
      const data = response.data;
      if (!data || typeof data !== 'object') {
        setUser(null);
        return false;
      }
      setUser(data);
      return true;
    } catch (err) {
      if (err.response?.status === 401) {
        // accessToken 만료 등으로 /users/me가 401일 수 있음 -> refresh 1회 시도 후 재조회
        if (!isRefreshingRef.current) {
          isRefreshingRef.current = true;
          try {
            const refreshed = await apiClient
              .post('/auth/refresh')
              .then(() => true)
              .catch(() => false);

            if (refreshed) {
              try {
                const response2 = await userAPI.getProfile();
                if (response2?.status === 200 && response2.data) {
                  setUser(response2.data);
                  return true;
                }
              } catch (e2) {
                // ignore and fallthrough to logout state
              }
            }
          } finally {
            isRefreshingRef.current = false;
          }
        }

        setUser(null);
        return false;
      }
      console.error('Failed to fetch user:', err);
      setUser(null);
      return false;
    }
  }, []);

  useEffect(() => {
    if (isLoggingOutRef.current) {
      setLoading(false);
      setUser(null);
      return;
    }

    const init = async () => {
      setLoading(true);
      await fetchUser();
      setLoading(false);
    };
    init();
  }, [fetchUser]);

  const login = useCallback(async () => {
    isLoggingOutRef.current = false;
    return await fetchUser();
  }, [fetchUser]);

  const logout = useCallback(async () => {
    isLoggingOutRef.current = true;
    try {
      await apiClient.post('/auth/logout');
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      setUser(null);
    }
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading,
      isAuthenticated: !!user,
      login,
      logout,
      refreshUser: fetchUser,
      setUser, // 내부에서만 쓰되, 필요 시 확장 가능
    }),
    [user, loading, login, logout, fetchUser]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within <AuthProvider />');
  }
  return ctx;
}


