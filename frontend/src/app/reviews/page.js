import { Suspense } from 'react';
import ReviewsClient from './ReviewsClient';

export default function ReviewsPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center">
          <p className="text-sm text-gray-600">페이지 로딩 중...</p>
        </div>
      }
    >
      <ReviewsClient />
    </Suspense>
  );
}

