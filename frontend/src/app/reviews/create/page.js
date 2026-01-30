import { Suspense } from 'react';
import CreateReviewClient from './CreateReviewClient';

export default function CreateReviewPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center">
          <p className="text-sm text-gray-600">페이지 로딩 중...</p>
        </div>
      }
    >
      <CreateReviewClient />
    </Suspense>
  );
}

