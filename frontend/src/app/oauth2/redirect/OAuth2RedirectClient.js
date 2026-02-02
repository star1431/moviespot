'use client';

import { useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { userAPI } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

const needsNicknameSetup = (u) => {
  const nick = u?.nickname || '';
  return nick.startsWith('ms_');
};

export default function OAuth2RedirectClient() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { refreshUser } = useAuth();
  const error = searchParams.get('error');

  useEffect(() => {
    const handleOAuth2Callback = async () => {
      if (error) {
        router.push(`/login?error=${encodeURIComponent(error)}`);
        return;
      }

      // OAuth2 성공 후 사용자 정보 새로고침 (쿠키가 자동으로 전송됨)
      try {
        const success = await refreshUser();
        if (success) {
          const me = await userAPI.getProfile().then((r) => r.data).catch(() => null);
          if (needsNicknameSetup(me)) {
            router.push('/onboarding/nickname');
          } else {
            router.push('/');
          }
        } else {
          router.push('/login?error=oauth_failed');
        }
      } catch (err) {
        console.error('Failed to refresh user after OAuth2:', err);
        router.push('/login?error=oauth_failed');
      }
    };

    handleOAuth2Callback();
  }, [error, router, refreshUser]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-center">
        <LoadingSpinner size="lg" />
        <p className="mt-4 text-gray-600">로그인 처리 중...</p>
      </div>
    </div>
  );
}


