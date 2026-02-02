'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { userAPI } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';

const needsNicknameSetup = (u) => {
  const nick = u?.nickname || '';
  return nick.startsWith('ms_');
};

export default function NicknameOnboardingPage() {
  const router = useRouter();
  const { user, loading, isAuthenticated, refreshUser } = useAuth();

  const [nickname, setNickname] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push('/login');
      return;
    }
    if (!loading && isAuthenticated && user && !needsNicknameSetup(user)) {
      router.push('/');
      return;
    }
    setNickname('');
  }, [loading, isAuthenticated, router, user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const next = nickname.trim();
    if (!next) {
      setError('닉네임을 입력해주세요.');
      return;
    }

    try {
      setSaving(true);
      await userAPI.updateProfile({ nickname: next });
      await refreshUser?.();
      router.push('/');
    } catch (err) {
      console.error('Failed to set nickname:', err);
      setError(err.response?.data?.error || '닉네임 설정에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4 py-12">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-lg">
        <h1 className="mb-2 text-center text-2xl font-bold text-gray-900">닉네임 설정</h1>
        <p className="mb-6 text-center text-sm text-gray-600">
          처음 로그인하셨네요. 사용할 닉네임을 설정해주세요.
        </p>

        {error && (
          <div className="mb-4 rounded-lg bg-red-50 p-4 text-sm text-red-800">{error}</div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="닉네임"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            placeholder="닉네임을 입력하세요"
            required
          />

          <Button type="submit" className="w-full" disabled={saving}>
            {saving ? <LoadingSpinner size="sm" /> : '완료'}
          </Button>
        </form>
      </div>
    </div>
  );
}


