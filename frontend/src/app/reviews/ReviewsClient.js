'use client';

import { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { reviewAPI } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import { getImageUrl, formatDate } from '@/lib/utils';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import Input from '@/components/ui/Input';
import { Heart, Eye, Star, Plus } from 'lucide-react';

export default function ReviewsClient() {
  const { isAuthenticated } = useAuth();
  const searchParams = useSearchParams();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [filters, setFilters] = useState({
    sort: searchParams.get('sort') || 'latest',
    keyword: searchParams.get('keyword') || '',
    tmdbId: searchParams.get('tmdbId') || '',
  });
  const size = 10;

  useEffect(() => {
    loadReviews();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, filters.sort, filters.keyword, filters.tmdbId]);

  const loadReviews = async () => {
    try {
      setLoading(true);
      const params = {
        page,
        size,
        sort: filters.sort,
      };
      if (filters.keyword) params.keyword = filters.keyword;
      if (filters.tmdbId) params.tmdbId = parseInt(filters.tmdbId);

      const response = await reviewAPI.getList(params);
      setReviews(response.data.content || []);
      setHasNext(response.data.hasNext || false);
    } catch (err) {
      console.error('Failed to load reviews:', err);
      setReviews([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">리뷰</h1>
          {isAuthenticated && (
            <Link href="/reviews/create">
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                리뷰 작성
              </Button>
            </Link>
          )}
        </div>

        {/* 필터 */}
        <div className="mb-6 rounded-lg bg-white p-6 shadow-sm">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <Select
              label="정렬"
              value={filters.sort}
              onChange={(e) => handleFilterChange('sort', e.target.value)}
              options={[
                { value: 'latest', label: '최신순' },
                { value: 'popular', label: '인기순' },
                { value: 'likes', label: '좋아요순' },
              ]}
            />
            <Input
              label="키워드 검색"
              placeholder="데이트, 혼자, 힐링 등"
              value={filters.keyword}
              onChange={(e) => handleFilterChange('keyword', e.target.value)}
            />
            <Input
              label="영화 ID (선택)"
              placeholder="TMDB ID"
              value={filters.tmdbId}
              onChange={(e) => handleFilterChange('tmdbId', e.target.value)}
            />
          </div>
        </div>

        {/* 리뷰 목록 */}
        {loading ? (
          <div className="flex justify-center py-12">
            <LoadingSpinner size="lg" />
          </div>
        ) : reviews.length === 0 ? (
          <div className="rounded-lg bg-white p-12 text-center shadow-sm">
            <p className="text-gray-500">리뷰가 없습니다.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {reviews.map((review) => (
              <Link
                key={review.reviewId}
                href={`/reviews/${review.reviewId}`}
                className="block rounded-lg bg-white p-6 shadow-sm transition-shadow hover:shadow-md"
              >
                <div className="flex gap-4">
                  {/* 영화 포스터 */}
                  {review.movie && (
                    <div className="relative h-32 w-24 flex-shrink-0 overflow-hidden rounded-lg">
                      <Image
                        src={getImageUrl(review.movie.posterUrl, 'w200')}
                        alt={review.movie.title}
                        fill
                        className="object-cover"
                      />
                    </div>
                  )}

                  {/* 리뷰 내용 */}
                  <div className="flex-1">
                    <div className="mb-2 flex items-start justify-between">
                      <div>
                        <h3 className="mb-1 text-lg font-bold text-gray-900">{review.title}</h3>
                        {review.movie && <p className="text-sm text-gray-600">{review.movie.title}</p>}
                      </div>
                      <div className="flex items-center gap-1">
                        <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                        <span className="text-sm font-medium">{review.score}점</span>
                      </div>
                    </div>

                    <p className="mb-3 line-clamp-2 text-sm text-gray-700">{review.content}</p>

                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-4 text-xs text-gray-500">
                        <span>{review.author.nickname}</span>
                        <span>{formatDate(review.createdAt)}</span>
                        <div className="flex items-center gap-1">
                          <Eye className="h-3 w-3" />
                          <span>{review.viewCount}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Heart className="h-3 w-3" />
                          <span>{review.likeCount}</span>
                        </div>
                      </div>

                      {review.keywords && review.keywords.length > 0 && (
                        <div className="flex gap-1">
                          {review.keywords.map((keyword, idx) => (
                            <span key={idx} className="rounded-full bg-blue-100 px-2 py-1 text-xs text-blue-800">
                              {keyword}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </Link>
            ))}

            {/* 페이지네이션 */}
            <div className="mt-8 flex items-center justify-center gap-4">
              <Button variant="outline" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
                이전
              </Button>
              <span className="text-sm text-gray-600">{page + 1} 페이지</span>
              <Button variant="outline" onClick={() => setPage((p) => p + 1)} disabled={!hasNext}>
                다음
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}


