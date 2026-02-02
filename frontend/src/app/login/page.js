import { Suspense } from 'react';
import LoginClient from './LoginClient';

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center">
          <p className="text-sm text-gray-600">페이지 로딩 중...</p>
        </div>
      }
    >
      <LoginClient />
    </Suspense>
  );
}
