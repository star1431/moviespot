import { useState, useEffect, useCallback, useRef } from 'react';
import { userAPI, apiClient } from '@/lib/api';

// 전역 인증 훅
// - /api/users/me 호출 결과가 200일 때만 로그인 성공으로 간주
// - 200이 아니면 모두 비로그인 상태로 처리
export const useAuth = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const isLoggingOutRef = useRef(false);

  const fetchUser = useCallback(async () => {
    // 로그아웃 처리 중이면 사용자 정보를 가져오지 않음
    if (isLoggingOutRef.current) {
      return false;
    }

    try {
      const response = await userAPI.getProfile();

      // 1) HTTP 200 이 아닌 경우 → 로그인 실패로 간주
      if (!response || response.status !== 200) {
        setUser(null);
        return false;
      }

      // 2) 응답 데이터가 객체 형태(정상 유저 정보)가 아니면 로그인 실패
      const data = response.data;
      if (!data || typeof data !== 'object') {
        setUser(null);
        return false;
      }

      // 여기까지 통과하면 로그인 성공으로 간주
      setUser(data);
      return true;
    } catch (error) {
      // 401 에러는 정상적인 비로그인 상태
      if (error.response?.status === 401) {
        setUser(null);
        return false;
      }

      // 그 외 에러도 일단 비로그인 처리
      console.error('Failed to fetch user:', error);
      setUser(null);
      return false;
    }
  }, []);

  useEffect(() => {
    // 로그아웃 중이면 초기화하지 않음
    if (isLoggingOutRef.current) {
      setLoading(false);
      setUser(null);
      return;
    }

    const initAuth = async () => {
      setLoading(true);
      await fetchUser();
      setLoading(false);
    };

    initAuth();
  }, [fetchUser]);

  const login = async () => {
    isLoggingOutRef.current = false;
    const success = await fetchUser();
    return success;
  };

  const logout = async () => {
    isLoggingOutRef.current = true;

    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
      }
    }
  };

  return {
    user,
    loading,
    isAuthenticated: !!user,
    login,
    logout,
    refreshUser: fetchUser,
  };
};

