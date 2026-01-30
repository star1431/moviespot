'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { userAPI } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';

export default function ProfilePage() {
  const router = useRouter();
  const { user, loading, isAuthenticated, refreshUser } = useAuth();

  const [nickname, setNickname] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push('/login');
      return;
    }
    setNickname(user?.nickname || '');
  }, [loading, isAuthenticated, router, user]);

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const next = nickname.trim();
    if (!next) {
      setError('닉네임을 입력해주세요.');
      return;
    }

    try {
      setSaving(true);
      await userAPI.updateProfile({ nickname: next });
      await refreshUser?.();
      setSuccess('프로필이 저장되었습니다.');
    } catch (err) {
      console.error('Failed to update profile:', err);
      setError(err.response?.data?.error || '프로필 저장에 실패했습니다.');
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
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-10">
        <div className="mx-auto max-w-xl rounded-lg bg-white p-8 shadow-sm">
          <h1 className="mb-2 text-2xl font-bold text-gray-900">나의 프로필</h1>
          <p className="mb-6 text-sm text-gray-600">닉네임을 수정할 수 있습니다.</p>

          {(error || success) && (
            <div
              className={`mb-4 rounded-lg p-4 text-sm ${
                error ? 'bg-red-50 text-red-800' : 'bg-green-50 text-green-800'
              }`}
            >
              {error || success}
            </div>
          )}

          <div className="mb-6 rounded-lg border border-gray-200 bg-gray-50 p-4 text-sm text-gray-700">
            <div className="flex justify-between gap-4">
              <span className="text-gray-500">이메일</span>
              <span className="font-medium">{user?.email || '-'}</span>
            </div>
            <div className="mt-2 flex justify-between gap-4">
              <span className="text-gray-500">로그인 방식</span>
              <span className="font-medium">{user?.provider || '-'}</span>
            </div>
          </div>

          <form onSubmit={handleSave} className="space-y-4">
            <Input
              label="닉네임"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="닉네임을 입력하세요"
              required
            />

            <div className="flex gap-2">
              <Button type="submit" disabled={saving}>
                {saving ? <LoadingSpinner size="sm" /> : '저장'}
              </Button>
              <Button type="button" variant="secondary" onClick={() => router.back()} disabled={saving}>
                돌아가기
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}


