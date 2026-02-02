'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { reviewAPI } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import { getImageUrl, formatDate } from '@/lib/utils';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { Heart, Eye, Star, ArrowLeft, Edit, Trash2 } from 'lucide-react';

export default function ReviewDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const reviewId = parseInt(params.reviewId);

  const [review, setReview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isLiked, setIsLiked] = useState(false);

  useEffect(() => {
    loadReview();
  }, [reviewId]);

  const loadReview = async () => {
    try {
      setLoading(true);
      const response = await reviewAPI.getDetail(reviewId);
      setReview(response.data);
      setIsLiked(response.data.isLiked || false);
    } catch (error) {
      console.error('Failed to load review:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleLike = async () => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    try {
      await reviewAPI.toggleLike(reviewId);
      await loadReview();
    } catch (error) {
      console.error('Failed to toggle like:', error);
    }
  };

  const handleDelete = async () => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      await reviewAPI.delete(reviewId);
      router.push('/reviews');
    } catch (error) {
      console.error('Failed to delete review:', error);
      alert('삭제에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!review) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <p className="text-gray-500">리뷰를 찾을 수 없습니다.</p>
          <Link href="/reviews">
            <Button className="mt-4">리뷰 목록으로 돌아가기</Button>
          </Link>
        </div>
      </div>
    );
  }

  const isAuthor = isAuthenticated && user?.userId === review.author.userId;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <Link href="/reviews">
          <Button variant="ghost" className="mb-4">
            <ArrowLeft className="mr-2 h-4 w-4" />
            리뷰 목록으로
          </Button>
        </Link>

        <div className="rounded-lg bg-white p-8 shadow-sm">
          {/* 헤더 */}
          <div className="mb-6 flex items-start justify-between">
            <div className="flex-1">
              <h1 className="mb-2 text-3xl font-bold text-gray-900">{review.title}</h1>
              <div className="flex items-center gap-4 text-sm text-gray-600">
                <span>{review.author.nickname}</span>
                <span>{formatDate(review.createdAt)}</span>
                {review.updatedAt !== review.createdAt && (
                  <span className="text-gray-400">
                    (수정됨: {formatDate(review.updatedAt)})
                  </span>
                )}
              </div>
            </div>
            {isAuthor && (
              <div className="flex gap-2">
                <Link href={`/reviews/${reviewId}/edit`}>
                  <Button variant="outline" size="sm">
                    <Edit className="h-4 w-4 mr-1" />
                    수정
                  </Button>
                </Link>
                <Button variant="outline" size="sm" onClick={handleDelete}>
                  <Trash2 className="h-4 w-4 mr-1" />
                  삭제
                </Button>
              </div>
            )}
          </div>

          {/* 영화 정보 */}
          {review.movie && (
            <div className="mb-6 flex gap-4 rounded-lg border border-gray-200 p-4">
              <div className="relative h-32 w-24 flex-shrink-0 overflow-hidden rounded-lg">
                <Image
                  src={getImageUrl(review.movie.posterUrl, 'w200')}
                  alt={review.movie.title}
                  fill
                  className="object-cover"
                />
              </div>
              <div className="flex-1">
                <Link
                  href={`/movies/${review.movie.tmdbId}`}
                  className="text-lg font-bold text-gray-900 hover:text-blue-600"
                >
                  {review.movie.title}
                </Link>
                <div className="mt-2 flex items-center gap-1">
                  <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                  <span className="text-sm font-medium">{review.score}점</span>
                </div>
              </div>
            </div>
          )}

          {/* 키워드 */}
          {review.keywords && review.keywords.length > 0 && (
            <div className="mb-6 flex gap-2">
              {review.keywords.map((keyword, idx) => (
                <span
                  key={idx}
                  className="rounded-full bg-blue-100 px-3 py-1 text-sm text-blue-800"
                >
                  {keyword}
                </span>
              ))}
            </div>
          )}

          {/* 리뷰 내용 */}
          <div className="mb-6 whitespace-pre-wrap text-gray-700">
            {review.content}
          </div>

          {/* 통계 및 액션 */}
          <div className="flex items-center justify-between border-t border-gray-200 pt-6">
            <div className="flex items-center gap-6 text-sm text-gray-600">
              <div className="flex items-center gap-1">
                <Eye className="h-4 w-4" />
                <span>조회 {review.viewCount}</span>
              </div>
              <div className="flex items-center gap-1">
                <Heart className="h-4 w-4" />
                <span>좋아요 {review.likeCount}</span>
              </div>
            </div>
            <Button
              variant={isLiked ? 'primary' : 'outline'}
              onClick={handleToggleLike}
              disabled={!isAuthenticated}
            >
              <Heart
                className={`mr-2 h-4 w-4 ${isLiked ? 'fill-current' : ''}`}
              />
              {isLiked ? '좋아요 취소' : '좋아요'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

