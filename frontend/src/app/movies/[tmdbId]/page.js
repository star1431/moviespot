'use client';

import { useState, useEffect } from 'react';
import { useParams, useSearchParams } from 'next/navigation';
import Image from 'next/image';
import Link from 'next/link';
import { movieAPI, ratingAPI, watchedMovieAPI, reviewAPI, genreAPI } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import { getImageUrl, formatRating, formatDate } from '@/lib/utils';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { Star, Play, Calendar, Clock, Film, Heart, MessageSquare, X } from 'lucide-react';

export default function MovieDetailPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const { user, isAuthenticated } = useAuth();
  const tmdbId = parseInt(params.tmdbId);
  const commentPage = parseInt(searchParams.get('commentPage') || '0');

  const [movie, setMovie] = useState(null);
  const [genres, setGenres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showTrailer, setShowTrailer] = useState(false);
  const [rating, setRating] = useState({ score: '', comment: '', isRecommended: true });
  const [watchedScore, setWatchedScore] = useState('');
  const [isWatched, setIsWatched] = useState(false);

  useEffect(() => {
    loadGenres();
    loadMovieDetail();
  }, [tmdbId, commentPage, isAuthenticated]);

  const loadGenres = async () => {
    try {
      const response = await genreAPI.getGenres();
      setGenres(response.data || []);
    } catch (error) {
      console.error('Failed to load genres:', error);
    }
  };

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
        setWatchedScore(response.data.myWatchedScore?.toString() || '');
      }
    } catch (error) {
      console.error('Failed to load movie detail:', error);
      setError('서버와 통신 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  const getGenreNames = () => {
    if (!movie?.genreIds || !genres.length) return [];
    return movie.genreIds
      .map((id) => genres.find((g) => g.id === id)?.name)
      .filter(Boolean);
  };

  const getYouTubeVideoId = (url) => {
    if (!url) return null;
    const match = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&\n?#]+)/);
    return match ? match[1] : null;
  };

  const handleRatingSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) return;

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

  const handleWatchedSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) return;

    try {
      await watchedMovieAPI.createOrUpdate({
        tmdbId,
        score: watchedScore ? parseInt(watchedScore) : null,
      });
      setIsWatched(true);
      await loadMovieDetail();
      alert('본 영화로 등록되었습니다.');
    } catch (error) {
      console.error('Failed to submit watched movie:', error);
      alert('등록에 실패했습니다.');
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
                />
              </div>
            </div>
            <div className="flex-1 text-white">
              <h1 className="mb-4 text-4xl font-bold">{movie.title}</h1>
              <div className="mb-4 flex flex-wrap gap-4 text-sm">
                {movie.releaseDate && (
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    <span>{new Date(movie.releaseDate).getFullYear()}</span>
                  </div>
                )}
                {movie.runtime && (
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4" />
                    <span>{movie.runtime}분</span>
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
              </div>
              {/* 장르 표시 */}
              {genreNames.length > 0 && (
                <div className="mb-4 flex flex-wrap gap-2">
                  {genreNames.map((name, index) => (
                    <span
                      key={index}
                      className="rounded-full bg-white/20 px-3 py-1 text-xs text-white"
                    >
                      {name}
                    </span>
                  ))}
                </div>
              )}
              {movie.overview && (
                <p className="mb-6 text-gray-200">{movie.overview}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
          {/* 메인 콘텐츠 */}
          <div className="lg:col-span-2">
            {/* 트레일러 섹션 */}
            {movie.trailerUrl && videoId && (
              <div className="mb-8 rounded-lg bg-white p-6 shadow-sm">
                <div className="mb-4 flex items-center justify-between">
                  <h2 className="text-xl font-bold">트레일러</h2>
                  {showTrailer && (
                    <button
                      onClick={() => setShowTrailer(false)}
                      className="rounded-full p-1 text-gray-500 hover:bg-gray-100"
                    >
                      <X className="h-5 w-5" />
                    </button>
                  )}
                </div>
                {showTrailer ? (
                  <div className="relative aspect-video w-full overflow-hidden rounded-lg bg-black">
                    <iframe
                      src={`https://www.youtube.com/embed/${videoId}?autoplay=1`}
                      title="트레일러"
                      className="h-full w-full"
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                      allowFullScreen
                    />
                  </div>
                ) : (
                  <button
                    onClick={() => setShowTrailer(true)}
                    className="group relative aspect-video w-full overflow-hidden rounded-lg bg-black"
                  >
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="flex flex-col items-center gap-2 text-white">
                        <div className="rounded-full bg-red-600 p-4 transition-transform group-hover:scale-110">
                          <Play className="h-8 w-8 fill-white" />
                        </div>
                        <span className="text-sm font-medium">트레일러 보기</span>
                      </div>
                    </div>
                    {movie.posterUrl && (
                      <Image
                        src={getImageUrl(movie.posterUrl, 'w780')}
                        alt={movie.title}
                        fill
                        className="object-cover opacity-50"
                      />
                    )}
                  </button>
                )}
              </div>
            )}

            {/* 평점/코멘트 섹션 */}
            {isAuthenticated && (
              <div className="mb-8 rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 text-xl font-bold">내 평점/코멘트</h2>
                <form onSubmit={handleRatingSubmit} className="space-y-4">
                  <div className="flex items-center gap-4">
                    <label className="text-sm font-medium">평점</label>
                    <select
                      value={rating.score}
                      onChange={(e) =>
                        setRating({ ...rating, score: e.target.value })
                      }
                      className="rounded-lg border border-gray-300 px-3 py-2"
                      required
                    >
                      <option value="">선택</option>
                      {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
                        <option key={num} value={num}>
                          {num}점
                        </option>
                      ))}
                    </select>
                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={rating.isRecommended}
                        onChange={(e) =>
                          setRating({
                            ...rating,
                            isRecommended: e.target.checked,
                          })
                        }
                      />
                      <span className="text-sm">추천</span>
                    </label>
                  </div>
                  <Input
                    label="코멘트"
                    value={rating.comment}
                    onChange={(e) =>
                      setRating({ ...rating, comment: e.target.value })
                    }
                    placeholder="영화에 대한 생각을 남겨주세요"
                  />
                  <div className="flex gap-2">
                    <Button type="submit">등록</Button>
                    {movie.myScore && (
                      <Button
                        type="button"
                        variant="secondary"
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

            {/* 본 영화 등록 */}
            {isAuthenticated && (
              <div className="mb-8 rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 text-xl font-bold">본 영화 등록</h2>
                <form onSubmit={handleWatchedSubmit} className="space-y-4">
                  <div className="flex items-center gap-4">
                    <label className="text-sm font-medium">평점 (선택)</label>
                    <select
                      value={watchedScore}
                      onChange={(e) => setWatchedScore(e.target.value)}
                      className="rounded-lg border border-gray-300 px-3 py-2"
                    >
                      <option value="">없음</option>
                      {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
                        <option key={num} value={num}>
                          {num}점
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="flex gap-2">
                    {!isWatched ? (
                      <Button type="submit">등록</Button>
                    ) : (
                      <>
                        <Button type="button" variant="secondary" disabled>
                          이미 등록된 영화입니다
                        </Button>
                        <Button
                          type="button"
                          variant="secondary"
                          onClick={async () => {
                            try {
                              await watchedMovieAPI.delete(tmdbId);
                              setIsWatched(false);
                              setWatchedScore('');
                              await loadMovieDetail();
                            } catch (error) {
                              console.error('Failed to delete watched movie:', error);
                            }
                          }}
                        >
                          삭제
                        </Button>
                      </>
                    )}
                  </div>
                </form>
              </div>
            )}

            {/* 사용자 평점 목록 */}
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <h2 className="mb-4 text-xl font-bold">사용자 평점</h2>
              {movie.userRatings?.content?.length > 0 ? (
                <div className="space-y-4">
                  {movie.userRatings.content.map((userRating, index) => (
                    <div key={index} className="border-b border-gray-200 pb-4 last:border-0">
                      <div className="mb-2 flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className="font-medium">{userRating.nickname}</span>
                          <div className="flex items-center gap-1">
                            <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                            <span className="text-sm">{userRating.score}점</span>
                          </div>
                          {userRating.isRecommended && (
                            <span className="rounded-full bg-blue-100 px-2 py-1 text-xs text-blue-800">
                              추천
                            </span>
                          )}
                        </div>
                        <span className="text-xs text-gray-500">
                          {formatDate(userRating.createdAt)}
                        </span>
                      </div>
                      {userRating.comment && (
                        <p className="text-sm text-gray-700">{userRating.comment}</p>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">아직 평점이 없습니다.</p>
              )}
            </div>
          </div>

          {/* 사이드바 */}
          <div className="space-y-6">
            {/* 리뷰 링크 */}
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <h3 className="mb-4 text-lg font-bold">리뷰</h3>
              <div className="space-y-2">
                <Link href={`/reviews?tmdbId=${tmdbId}`}>
                  <Button variant="outline" className="w-full">
                    <MessageSquare className="mr-2 h-4 w-4" />
                    리뷰 보기
                  </Button>
                </Link>
                {isAuthenticated && (
                  <Link href={`/reviews/write?tmdbId=${tmdbId}`}>
                    <Button className="w-full">
                      리뷰 작성하기
                    </Button>
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
