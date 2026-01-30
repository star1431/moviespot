'use client';

import { useState, useEffect } from 'react';
import { useParams, useSearchParams, useRouter } from 'next/navigation';
import Image from 'next/image';
import Link from 'next/link';
import { movieAPI, ratingAPI, watchedMovieAPI, reviewAPI } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import { getImageUrl, formatRating, formatDate } from '@/lib/utils';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { Star, Calendar, Clock, MessageSquare, BookOpen, Eye } from 'lucide-react';

export default function MovieDetailPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { isAuthenticated, refreshUser, loading: authLoading } = useAuth();
  const tmdbId = parseInt(params.tmdbId);
  const commentPage = parseInt(searchParams.get('commentPage') || '0');

  const [movie, setMovie] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [reviewCount, setReviewCount] = useState(0);
  const [movieKeywords, setMovieKeywords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [rating, setRating] = useState({ score: '', comment: '', isRecommended: true });
  const [isWatched, setIsWatched] = useState(false);

  useEffect(() => {
    loadMovieDetail();
    loadReviews();
  }, [tmdbId, commentPage, isAuthenticated]);

  const loadMovieDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await movieAPI.getMovieDetail(tmdbId, commentPage, 10);
      setMovie(response.data);
      if (isAuthenticated && response.data.myScore) {
        setRating({
          score: response.data.myScore.toString(),
          comment: response.data.myComment || '',
          isRecommended: response.data.myRecommended ?? true,
        });
        setIsWatched(response.data.isWatched);
      }
    } catch (error) {
      console.error('Failed to load movie detail:', error);
      setError('서버와 통신 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  const loadReviews = async () => {
    try {
      const response = await reviewAPI.getList({ tmdbId, page: 0, size: 5 });
      const reviewsData = response.data.content || [];
      setReviews(reviewsData);
      
      // 리뷰 개수 계산
      const total = response.data.totalElements ?? response.data.total ?? reviewsData.length;
      setReviewCount(total);
      
      // 영화의 관심키워드 추출 (리뷰에서 사용된 키워드)
      const keywords = new Set();
      reviewsData.forEach(review => {
        if (review.keywords && Array.isArray(review.keywords)) {
          review.keywords.forEach(kw => keywords.add(kw));
        }
      });
      setMovieKeywords(Array.from(keywords));
    } catch (error) {
      console.error('Failed to load reviews:', error);
      setReviews([]);
      setReviewCount(0);
      setMovieKeywords([]);
    }
  };

  const getGenreNames = () => {
    // TMDB genres(id,name)를 백엔드에서 그대로 내려주므로, 프론트는 name만 꺼내 표시한다.
    if (!Array.isArray(movie?.genres) || movie.genres.length === 0) return [];
    return movie.genres.map((g) => g?.name).filter((n) => n);
  };

  const getYouTubeVideoId = (url) => {
    if (!url) return null;
    const match = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&\n?#]+)/);
    return match ? match[1] : null;
  };

  const ensureAuthenticated = async () => {
    // "화면상 로그인"과 "서버에서 인증됨"은 다를 수 있음(토큰 만료/쿠키 누락 등).
    // 따라서 클릭 액션에서는 항상 /users/me(refreshUser) 결과로 인증 여부를 확정한다.
    if (authLoading) {
      // 로딩 중이면 한번 더 동기화 시도
      return (await refreshUser?.()) === true;
    }
    return (await refreshUser?.()) === true;
  };

  const handleRatingSubmit = async (e) => {
    e.preventDefault();
    const ok = await ensureAuthenticated();
    if (!ok) {
      router.push('/login');
      return;
    }

    try {
      await ratingAPI.createOrUpdate({
        tmdbId,
        score: parseInt(rating.score),
        comment: rating.comment,
        isRecommended: rating.isRecommended,
      });
      await loadMovieDetail();
      alert('평점이 등록되었습니다.');
    } catch (error) {
      console.error('Failed to submit rating:', error);
      alert('평점 등록에 실패했습니다.');
    }
  };

  const handleWatchedClick = async () => {
    const ok = await ensureAuthenticated();
    console.log(ok);
    if (!ok) {
      router.push('/login');
      return;
    }

    if (isWatched) {
      try {
        await watchedMovieAPI.delete(tmdbId);
        setIsWatched(false);
        await loadMovieDetail();
      } catch (error) {
        console.error('Failed to delete watched movie:', error);
        if (error.response?.status === 401) {
          const ok2 = await refreshUser?.();
          if (ok2) {
            // 토큰 재발급 후 1회 재시도
            await watchedMovieAPI.delete(tmdbId);
            setIsWatched(false);
            await loadMovieDetail();
            return;
          }
          router.push('/login');
        }
      }
    } else {
      try {
        await watchedMovieAPI.createOrUpdate({
          tmdbId,
        });
        setIsWatched(true);
        await loadMovieDetail();
        alert('본 영화로 등록되었습니다.');
      } catch (error) {
        console.error('Failed to submit watched movie:', error);
        if (error.response?.status === 401) {
          const ok2 = await refreshUser?.();
          if (ok2) {
            // 토큰 재발급 후 1회 재시도
            await watchedMovieAPI.createOrUpdate({
              tmdbId,
            });
            setIsWatched(true);
            await loadMovieDetail();
            alert('본 영화로 등록되었습니다.');
            return;
          }
          router.push('/login');
        } else {
          alert('등록에 실패했습니다.');
        }
      }
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="mb-4 rounded-lg bg-red-50 p-4">
            <p className="text-red-600 font-semibold">{error}</p>
          </div>
          <Link href="/movies">
            <Button className="mt-4">영화 목록으로 돌아가기</Button>
          </Link>
        </div>
      </div>
    );
  }

  if (!movie) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <p className="text-gray-500">영화 정보를 찾을 수 없습니다.</p>
          <Link href="/movies">
            <Button className="mt-4">영화 목록으로 돌아가기</Button>
          </Link>
        </div>
      </div>
    );
  }

  const genreNames = getGenreNames();
  const videoId = getYouTubeVideoId(movie.trailerUrl);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 영화 헤더 */}
      <div className="relative bg-gradient-to-r from-gray-900 to-gray-700">
        <div className="container mx-auto px-4 py-12">
          <div className="flex flex-col gap-8 md:flex-row">
            <div className="flex-shrink-0">
              <div className="relative aspect-[2/3] w-64 overflow-hidden rounded-lg shadow-lg">
                <Image
                  src={getImageUrl(movie.posterUrl, 'w500')}
                  alt={movie.title}
                  fill
                  className="object-cover"
                  sizes="(max-width: 768px) 50vw, 256px"
                />
              </div>
            </div>
            <div className="flex-1 text-white">
              <div className="mb-4 flex items-start justify-between gap-4">
                <h1 className="text-4xl font-bold">{movie.title}</h1>
                {/* 본 영화 등록 버튼 - 제목 우측 끝 */}
                {isAuthenticated && (
                  <button
                    onClick={handleWatchedClick}
                    className={`flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                      isWatched
                        ? 'bg-green-600 hover:bg-green-700'
                        : 'bg-white/20 hover:bg-white/30'
                    }`}
                  >
                    <Eye className="h-4 w-4" />
                    {isWatched ? '본 영화' : '본 영화 등록'}
                  </button>
                )}
              </div>
              <div className="mb-4 flex flex-wrap gap-4 text-sm">
                {movie.releaseDate && (
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    <span>{new Date(movie.releaseDate).getFullYear()}</span>
                  </div>
                )}
                <div className="flex items-center gap-2">
                  <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                  <span>{formatRating(movie.voteAverage)}</span>
                  {movie.userAverageRating && (
                    <span className="text-gray-300">
                      (우리회원 {formatRating(movie.userAverageRating)})
                    </span>
                  )}
                </div>
                {movie.runtime && (
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4" />
                    <span>{movie.runtime}분</span>
                  </div>
                )}
              </div>
              {/* 장르 표시 - 다음 행 */}
              {genreNames.length > 0 && (
                <div className="mb-4 flex flex-wrap gap-2">
                  {genreNames.map((name, index) => (
                    <span
                      key={`${name}-${index}`}
                      className="rounded-full bg-white/20 px-3 py-1 text-xs font-medium text-white"
                    >
                      {name}
                    </span>
                  ))}
                </div>
              )}
              {movie.overview && (
                <p className="mb-6 text-gray-200 leading-relaxed">{movie.overview}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        {/* 상단 2컬럼: 트레일러 | 관심키워드+리뷰 */}
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-3 mb-8">
          {/* 왼쪽: 트레일러 */}
          <div className="lg:col-span-2">
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <div className="mb-4 flex items-center justify-between">
                <h2 className="text-xl font-bold text-gray-900">트레일러</h2>
              </div>

              {videoId ? (
                <div className="relative aspect-video w-full overflow-hidden rounded-lg bg-black">
                  <iframe
                    src={`https://www.youtube.com/embed/${videoId}`}
                    title="트레일러"
                    className="h-full w-full"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                  />
                </div>
              ) : (
                <div className="flex aspect-video w-full items-center justify-center rounded-lg border border-dashed border-gray-300 bg-gray-50">
                  <p className="text-sm text-gray-600">해당 영화 트레일러가 없습니다</p>
                </div>
              )}
            </div>
          </div>

          {/* 오른쪽: 관심 키워드 + 리뷰 */}
          <div className="space-y-8 lg:col-span-1">
            {/* 관심 키워드 */}
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <div className="mb-4 flex items-center gap-2">
                <BookOpen className="h-5 w-5 text-purple-600" />
                <h2 className="text-xl font-bold text-gray-900">관심 키워드</h2>
              </div>
              {movieKeywords.length > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {movieKeywords.map((keyword, index) => (
                    <span
                      key={index}
                      className="rounded-full bg-purple-100 px-3 py-1 text-sm font-medium text-purple-800"
                    >
                      {keyword}
                    </span>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-gray-500">아직 관심 키워드가 없습니다</p>
              )}
            </div>

            {/* 리뷰 */}
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <div className="mb-4 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <MessageSquare className="h-5 w-5 text-blue-600" />
                  <h2 className="text-xl font-bold text-gray-900">리뷰</h2>
                  <span className="text-sm text-gray-500">({reviewCount}건)</span>
                </div>
                {isAuthenticated && (
                  <Link href={`/reviews/create?tmdbId=${tmdbId}`}>
                    <Button size="sm">리뷰 작성</Button>
                  </Link>
                )}
              </div>
              {reviews.length > 0 ? (
                <div className="space-y-4">
                  {reviews.map((review) => (
                    <Link
                      key={review.reviewId}
                      href={`/reviews/${review.reviewId}`}
                      className="block rounded-lg border border-gray-200 p-4 transition-colors hover:bg-gray-50"
                    >
                      <div className="mb-2 flex items-start justify-between">
                        <div className="flex-1">
                          <h3 className="mb-1 font-semibold text-gray-900">{review.title}</h3>
                          <p className="mb-2 line-clamp-2 text-sm text-gray-600">{review.content}</p>
                          <div className="flex items-center gap-4 text-xs text-gray-500">
                            <span>{review.user?.nickname || '익명'}</span>
                            <div className="flex items-center gap-1">
                              <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                              <span>{review.score}점</span>
                            </div>
                            <span>조회 {review.viewCount}</span>
                            <span>좋아요 {review.likeCount}</span>
                            <span>{formatDate(review.createdAt)}</span>
                          </div>
                        </div>
                      </div>
                    </Link>
                  ))}
                  {reviewCount > 5 && (
                    <div className="pt-4">
                      <Link href={`/reviews?tmdbId=${tmdbId}`}>
                        <Button variant="outline" className="w-full">
                          리뷰 더보기
                        </Button>
                      </Link>
                    </div>
                  )}
                </div>
              ) : (
                <div className="py-8 text-center">
                  <p className="mb-4 text-sm text-gray-500">아직 리뷰가 없습니다</p>
                  {isAuthenticated && (
                    <Link href={`/reviews/create?tmdbId=${tmdbId}`}>
                      <Button size="sm">첫 리뷰 작성하기</Button>
                    </Link>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 하단: 사용자 평점 + 내 평점/코멘트 */}
        <div className="space-y-8">
          {/* 사용자 평점 목록 */}
          <div className="rounded-lg bg-white p-6 shadow-sm">
            <h2 className="mb-4 text-xl font-bold text-gray-900">사용자 평점</h2>
            {movie.userRatings?.content?.length > 0 ? (
              <div className="space-y-4">
                {movie.userRatings.content.map((userRating, index) => (
                  <div key={index} className="border-b border-gray-200 pb-4 last:border-0">
                    <div className="mb-2 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-gray-900">{userRating.nickname}</span>
                        <div className="flex items-center gap-1">
                          <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                          <span className="text-sm font-medium">{userRating.score}점</span>
                        </div>
                        {userRating.isRecommended && (
                          <span className="rounded-full bg-blue-100 px-2 py-1 text-xs font-medium text-blue-800">
                            추천
                          </span>
                        )}
                      </div>
                      <span className="text-xs text-gray-500">
                        {formatDate(userRating.createdAt)}
                      </span>
                    </div>
                    {userRating.comment && (
                      <p className="text-sm text-gray-700 leading-relaxed">{userRating.comment}</p>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">아직 평점이 없습니다.</p>
            )}
          </div>

          {/* 내 평점/코멘트 - 사용자 평점 섹션 밑에 */}
          {isAuthenticated && (
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <h2 className="mb-4 text-xl font-bold text-gray-900">내 평점/코멘트</h2>
              <form onSubmit={handleRatingSubmit} className="space-y-4">
                <div className="flex items-center gap-4">
                  <label className="text-sm font-medium text-gray-700">평점</label>
                  <select
                    value={rating.score}
                    onChange={(e) =>
                      setRating({ ...rating, score: e.target.value })
                    }
                    className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    required
                  >
                    <option value="">선택</option>
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
                      <option key={num} value={num}>
                        {num}점
                      </option>
                    ))}
                  </select>
                  <label className="flex items-center gap-2 text-sm text-gray-700">
                    <input
                      type="checkbox"
                      checked={rating.isRecommended}
                      onChange={(e) =>
                        setRating({
                          ...rating,
                          isRecommended: e.target.checked,
                        })
                      }
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span>추천</span>
                  </label>
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-gray-700">
                    코멘트
                  </label>
                  <textarea
                    value={rating.comment}
                    onChange={(e) =>
                      setRating({ ...rating, comment: e.target.value })
                    }
                    placeholder="영화에 대한 생각을 남겨주세요"
                    rows={4}
                    className="w-full rounded-lg border border-gray-300 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                  />
                </div>
                <div className="flex gap-2">
                  <Button type="submit" size="sm">
                    {movie.myScore ? '수정' : '등록'}
                  </Button>
                  {movie.myScore && (
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      onClick={async () => {
                        try {
                          await ratingAPI.delete(tmdbId);
                          setRating({ score: '', comment: '', isRecommended: true });
                          await loadMovieDetail();
                        } catch (error) {
                          console.error('Failed to delete rating:', error);
                        }
                      }}
                    >
                      삭제
                    </Button>
                  )}
                </div>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
