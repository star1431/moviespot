'use client';

import { useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
import { apiClient } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { Film } from 'lucide-react';

export default function LoginClient() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { isAuthenticated, refreshUser } = useAuth();
  const error = searchParams.get('error');

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/');
    }
  }, [isAuthenticated, router]);

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setFormError('');
  };

  const handleEmailLogin = async (e) => {
    e.preventDefault();
    setFormError('');
    setLoading(true);

    try {
      await apiClient.post('/auth/login', {
        email: formData.email,
        password: formData.password,
      });

      // 로그인 성공 후 사용자 정보 가져오기 (쿠키가 자동으로 전송됨)
      const success = await refreshUser();
      if (success) {
        router.push('/');
      } else {
        setFormError('로그인에 성공했지만 사용자 정보를 가져오는데 실패했습니다.');
      }
    } catch (err) {
      console.error('Login error:', err);
      setFormError(
        err.response?.data?.error || '로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSocialLogin = (provider) => {
    const RAW_BASE =
      process.env.REACT_APP_API_BASE_URL ||
      'http://localhost:8080';
    // 백엔드 OAuth 엔드포인트로 리다이렉트 (origin 기준)
    window.location.href = `${RAW_BASE}/oauth2/authorization/${provider}`;
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4 py-12">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-lg">
        <div className="mb-6 flex justify-center">
          <Film className="h-16 w-16 text-blue-600" />
        </div>
        <h1 className="mb-2 text-center text-2xl font-bold text-gray-900">MovieSpot 로그인</h1>
        <p className="mb-8 text-center text-gray-600">영화 컬렉션을 관리하려면 로그인이 필요합니다</p>

        {(error || formError) && (
          <div className="mb-4 rounded-lg bg-red-50 p-4 text-sm text-red-800">
            {formError || '로그인에 실패했습니다. 다시 시도해주세요.'}
          </div>
        )}

        {/* 이메일/비밀번호 로그인 폼 */}
        <form onSubmit={handleEmailLogin} className="mb-6 space-y-4">
          <Input
            label="이메일"
            type="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            placeholder="이메일을 입력하세요"
            required
          />
          <Input
            label="비밀번호"
            type="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            placeholder="비밀번호를 입력하세요"
            required
          />
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? <LoadingSpinner size="sm" /> : '로그인'}
          </Button>
        </form>

        {/* 구분선 */}
        <div className="relative mb-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-gray-300"></div>
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="bg-white px-2 text-gray-500">또는</span>
          </div>
        </div>

        {/* 소셜 로그인 버튼 */}
        <div className="space-y-3">
          <button
            type="button"
            onClick={() => handleSocialLogin('google')}
            className="flex w-full items-center justify-center gap-3 rounded-lg border border-gray-300 bg-white px-4 py-3 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              />
            </svg>
            Google로 로그인
          </button>

          <button
            type="button"
            onClick={() => handleSocialLogin('naver')}
            className="flex w-full items-center justify-center gap-3 rounded-lg bg-[#03C75A] px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-[#02B350]"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="currentColor">
              <path d="M16.273 12.845L7.376 0H0v24h7.726V11.156L16.624 24H24V0h-7.727v12.845z" />
            </svg>
            Naver로 로그인
          </button>

          <button
            type="button"
            onClick={() => handleSocialLogin('kakao')}
            className="flex w-full items-center justify-center gap-3 rounded-lg bg-[#FEE500] px-4 py-3 text-sm font-medium text-[#000000] transition-colors hover:bg-[#FDD835]"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 3c5.799 0 10.5 3.664 10.5 8.185 0 4.52-4.701 8.184-10.5 8.184a13.5 13.5 0 0 1-1.727-.11l-4.408 2.883c-.501.265-.678.236-.472-.413l.892-3.678c-2.88-1.46-4.785-3.99-4.785-6.866C1.5 6.665 6.201 3 12 3z" />
            </svg>
            Kakao로 로그인
          </button>
        </div>

        {/* 회원가입 링크 */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            아직 회원이 아니신가요?{' '}
            <Link href="/signup" className="font-medium text-blue-600 hover:text-blue-700">
              회원가입
            </Link>
          </p>
        </div>

        <p className="mt-6 text-center text-xs text-gray-500">
          로그인하면 MovieSpot의 서비스 약관 및 개인정보 처리방침에 동의하는 것으로 간주됩니다.
        </p>
      </div>
    </div>
  );
}


