import { Suspense } from 'react';
import OAuth2RedirectClient from './OAuth2RedirectClient';

export default function OAuth2RedirectPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center">
          <p className="text-sm text-gray-600">로그인 처리 중...</p>
        </div>
      }
    >
      <OAuth2RedirectClient />
    </Suspense>
  );
}

