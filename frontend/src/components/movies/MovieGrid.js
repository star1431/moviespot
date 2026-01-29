'use client';

import { useState, useEffect } from 'react';
import MovieCard from '@/components/ui/MovieCard';
import { MovieGridSkeleton } from '@/components/ui/Skeleton';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function MovieGrid({ fetchFunction, params = {} }) {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const size = 20;

  useEffect(() => {
    if (!fetchFunction) return;

    const loadMovies = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await fetchFunction({ ...params, page, size });
        setMovies(response.data.content || []);
        setHasNext(response.data.hasNext || false);
      } catch (error) {
        console.error('Failed to fetch movies:', error);
        setMovies([]);
        setError('서버와 통신 실패했습니다');
      } finally {
        setLoading(false);
      }
    };

    loadMovies();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchFunction, page, JSON.stringify(params)]);

  // fetchFunction 변경 시 페이지 초기화
  useEffect(() => {
    setPage(0);
  }, [fetchFunction]);

  const handlePrevPage = () => {
    if (page > 0) {
      setPage(page - 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleNextPage = () => {
    if (hasNext) {
      setPage(page + 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  if (loading) {
    return (
      <div className="py-12">
        <div className="flex items-center justify-center">
          <LoadingSpinner size="lg" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="py-12">
        <div className="rounded-lg bg-red-50 p-4">
          <p className="text-center text-red-600 font-semibold">{error}</p>
        </div>
      </div>
    );
  }

  if (movies.length === 0) {
    return (
      <div className="py-12 text-center">
        <p className="text-gray-500">영화를 찾을 수 없습니다.</p>
      </div>
    );
  }

  return (
    <div>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        {movies.map((movie, index) => (
          <MovieCard key={movie.id || movie.tmdbId || index} movie={movie} />
        ))}
      </div>

      {/* 페이지네이션 */}
      <div className="mt-8 flex items-center justify-center gap-4">
        <Button
          variant="outline"
          onClick={handlePrevPage}
          disabled={page === 0}
        >
          <ChevronLeft className="h-4 w-4 mr-1" />
          이전
        </Button>
        <span className="text-sm text-gray-600">
          {page + 1} 페이지
        </span>
        <Button
          variant="outline"
          onClick={handleNextPage}
          disabled={!hasNext}
        >
          다음
          <ChevronRight className="h-4 w-4 ml-1" />
        </Button>
      </div>
    </div>
  );
}

