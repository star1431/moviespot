'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { userAPI, genreAPI } from '@/lib/api';
import MovieCard from '@/components/ui/MovieCard';
import { MovieGridSkeleton } from '@/components/ui/Skeleton';
import Button from '@/components/ui/Button';
import { Skeleton } from '@/components/ui/Skeleton';
import { Film, Star, BookOpen, X, Plus, LogIn } from 'lucide-react';
import Link from 'next/link';

export default function CollectionPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [genres, setGenres] = useState([]);
  const [userGenres, setUserGenres] = useState([]);
  const [keywords, setKeywords] = useState([]);
  const [watchedMovies, setWatchedMovies] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newKeyword, setNewKeyword] = useState('');

  useEffect(() => {
    if (!authLoading) {
      if (isAuthenticated) {
        loadData();
      } else {
        setLoading(false);
      }
    }
  }, [isAuthenticated, authLoading]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [genresRes, userGenresRes, keywordsRes, watchedRes, reviewsRes] =
        await Promise.all([
          genreAPI.getGenres(),
          userAPI.getGenres(),
          userAPI.getKeywords(),
          userAPI.getWatchedMovies(0, 10),
          userAPI.getReviews(0, 10),
        ]);

      // 배열이 아닌 경우 빈 배열로 초기화
      setGenres(Array.isArray(genresRes.data) ? genresRes.data : []);
      setUserGenres(Array.isArray(userGenresRes.data) ? userGenresRes.data : []);
      setKeywords(Array.isArray(keywordsRes.data) ? keywordsRes.data : []);
      setWatchedMovies(Array.isArray(watchedRes.data?.content) ? watchedRes.data.content : []);
      setReviews(Array.isArray(reviewsRes.data?.content) ? reviewsRes.data.content : []);
    } catch (error) {
      console.error('Failed to load collection data:', error);
      // 에러 발생 시 빈 배열로 초기화
      setGenres([]);
      setUserGenres([]);
      setKeywords([]);
      setWatchedMovies([]);
      setReviews([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAddGenre = async (genreId) => {
    try {
      await userAPI.addGenre({ genreId });
      await loadData();
    } catch (error) {
      console.error('Failed to add genre:', error);
    }
  };

  const handleRemoveGenre = async (genreId) => {
    try {
      await userAPI.deleteGenre(genreId);
      await loadData();
    } catch (error) {
      console.error('Failed to remove genre:', error);
    }
  };

  const handleAddKeyword = async () => {
    if (!newKeyword.trim()) return;
    try {
      await userAPI.addKeyword({ keywordName: newKeyword.trim() });
      setNewKeyword('');
      await loadData();
    } catch (error) {
      console.error('Failed to add keyword:', error);
    }
  };

  const handleRemoveKeyword = async (keywordName) => {
    try {
      await userAPI.deleteKeyword({ keywordName });
      await loadData();
    } catch (error) {
      console.error('Failed to remove keyword:', error);
    }
  };

  const userGenreIds = Array.isArray(userGenres) ? userGenres.map((g) => g.id || g.genreId) : [];

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <Skeleton className="mb-8 h-10 w-64" />
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <Skeleton className="h-64" />
            <Skeleton className="h-64" />
            <Skeleton className="h-64" />
            <Skeleton className="h-64" />
          </div>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="flex min-h-[60vh] items-center justify-center">
            <div className="text-center">
              <Film className="mx-auto mb-4 h-16 w-16 text-gray-400" />
              <h2 className="mb-2 text-2xl font-bold text-gray-900">
                로그인이 필요한 서비스입니다
              </h2>
              <p className="mb-6 text-gray-600">
                나의 컬렉션을 확인하려면 로그인해주세요
              </p>
              <Link href="/login">
                <Button size="lg">
                  <LogIn className="mr-2 h-5 w-5" />
                  로그인하기
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <h1 className="mb-8 text-3xl font-bold text-gray-900">나의 컬렉션</h1>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* 관심 장르 */}
          <div className="rounded-lg bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center gap-2">
              <Star className="h-5 w-5 text-blue-600" />
              <h2 className="text-xl font-bold text-gray-900">관심 장르</h2>
            </div>
            <div className="mb-4 flex flex-wrap gap-2">
              {Array.isArray(userGenres) && userGenres.length > 0 ? (
                userGenres.map((genre) => {
                  const genreId = genre.id || genre.genreId;
                  return (
                    <span
                      key={genreId}
                      className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-3 py-1 text-sm text-blue-800"
                    >
                      {genre.name}
                      <button
                        onClick={() => handleRemoveGenre(genreId)}
                        className="hover:text-blue-900"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </span>
                  );
                })
              ) : (
                <p className="text-sm text-gray-500">관심 장르가 없습니다</p>
              )}
            </div>
            <div className="flex flex-wrap gap-2">
              {Array.isArray(genres) && genres.length > 0
                ? genres
                    .filter((genre) => !userGenreIds.includes(genre.id))
                    .slice(0, 10)
                    .map((genre) => (
                      <button
                        key={genre.id}
                        onClick={() => handleAddGenre(genre.id)}
                        className="rounded-full border border-gray-300 px-3 py-1 text-sm text-gray-700 transition-colors hover:bg-gray-50"
                      >
                        + {genre.name}
                      </button>
                    ))
                : null}
            </div>
          </div>

          {/* 관심 키워드 */}
          <div className="rounded-lg bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center gap-2">
              <BookOpen className="h-5 w-5 text-purple-600" />
              <h2 className="text-xl font-bold text-gray-900">관심 키워드</h2>
            </div>
            <div className="mb-4 flex flex-wrap gap-2">
              {Array.isArray(keywords) && keywords.length > 0 ? (
                keywords.map((keyword) => {
                  const keywordName = typeof keyword === 'string' ? keyword : (keyword.name || keyword.keywordName || '');
                  return (
                    <span
                      key={keywordName}
                      className="inline-flex items-center gap-1 rounded-full bg-purple-100 px-3 py-1 text-sm text-purple-800"
                    >
                      {keywordName}
                      <button
                        onClick={() => handleRemoveKeyword(keywordName)}
                        className="hover:text-purple-900"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </span>
                  );
                })
              ) : (
                <p className="text-sm text-gray-500">관심 키워드가 없습니다</p>
              )}
            </div>
            <div className="flex gap-2">
              <input
                type="text"
                value={newKeyword}
                onChange={(e) => setNewKeyword(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleAddKeyword()}
                placeholder="키워드 입력"
                className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-purple-500 focus:outline-none focus:ring-2 focus:ring-purple-500/20"
              />
              <Button onClick={handleAddKeyword}>
                <Plus className="h-4 w-4" />
              </Button>
            </div>
          </div>

          {/* 내가 본 영화 */}
          <div className="rounded-lg bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Film className="h-5 w-5 text-green-600" />
                <h2 className="text-xl font-bold text-gray-900">내가 본 영화</h2>
              </div>
              <Link href="/collection/watched">
                <Button variant="ghost" size="sm">
                  더보기
                </Button>
              </Link>
            </div>
            {watchedMovies.length > 0 ? (
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                {watchedMovies.slice(0, 5).map((movie) => (
                  <MovieCard key={movie.tmdbId} movie={movie} />
                ))}
              </div>
            ) : (
              <p className="py-8 text-center text-sm text-gray-500">
                본 영화가 없습니다
              </p>
            )}
          </div>

          {/* 내가 작성한 리뷰 */}
          <div className="rounded-lg bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <BookOpen className="h-5 w-5 text-orange-600" />
                <h2 className="text-xl font-bold text-gray-900">내가 작성한 리뷰</h2>
              </div>
              <Link href="/collection/reviews">
                <Button variant="ghost" size="sm">
                  더보기
                </Button>
              </Link>
            </div>
            {reviews.length > 0 ? (
              <div className="space-y-4">
                {reviews.slice(0, 5).map((review) => (
                  <Link
                    key={review.reviewId}
                    href={`/reviews/${review.reviewId}`}
                    className="block rounded-lg border border-gray-200 p-4 transition-colors hover:bg-gray-50"
                  >
                    <h3 className="mb-1 font-semibold text-gray-900">
                      {review.title}
                    </h3>
                    <p className="mb-2 line-clamp-2 text-sm text-gray-600">
                      {review.content}
                    </p>
                    <div className="flex items-center gap-4 text-xs text-gray-500">
                      <span>{review.movie?.title}</span>
                      <span>조회 {review.viewCount}</span>
                      <span>좋아요 {review.likeCount}</span>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <p className="py-8 text-center text-sm text-gray-500">
                작성한 리뷰가 없습니다
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

