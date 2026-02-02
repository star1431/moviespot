'use client';

import { useState, useEffect, useCallback } from 'react';
import Image from 'next/image';
import { movieAPI } from '@/lib/api';
import { getImageUrl } from '@/lib/utils';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { X, Search, Film } from 'lucide-react';

/**
 * 영화 검색 팝업 컴포넌트
 * @param {boolean} isOpen - 모달 열림 여부
 * @param {Function} onClose - 모달 닫기 콜백
 * @param {Function} onSelect - 영화 선택 콜백 (movie 객체 전달)
 * @param {number|null} initialTmdbId - 초기 선택된 영화 ID (영화상세에서 넘어온 경우)
 */
export default function MovieSearchModal({ isOpen, onClose, onSelect, initialTmdbId = null }) {
  const [searchKeyword, setSearchKeyword] = useState('');
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [initialMovie, setInitialMovie] = useState(null);

  const normalizeMovie = (m) => ({
    tmdbId: m?.tmdbId ?? m?.id, // MovieResponseDto: id 가 tmdbId
    title: m?.title,
    releaseDate: m?.releaseDate,
    posterUrl: m?.posterUrl,
  });

  // 모달이 열릴 때 초기화 및 초기 영화 로드
  useEffect(() => {
    if (isOpen) {
      setSearchKeyword('');
      setMovies([]);
      
      // initialTmdbId가 있으면 영화 정보 로드
      if (initialTmdbId && !initialMovie) {
        loadMovieDetail(initialTmdbId);
      } else if (initialMovie) {
        setSelectedMovie(initialMovie);
      } else {
        setSelectedMovie(null);
      }
    } else {
      // 모달이 닫힐 때 초기화
      setSearchKeyword('');
      setMovies([]);
      setSelectedMovie(null);
      setInitialMovie(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, initialTmdbId]);

  const loadMovieDetail = async (tmdbId) => {
    try {
      setLoading(true);
      const response = await movieAPI.getMovieDetail(tmdbId, 0, 1);
      const movie = normalizeMovie(response.data);
      setInitialMovie(movie);
      setSelectedMovie(movie);
    } catch (error) {
      console.error('Failed to load movie detail:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = useCallback(
    async (keyword) => {
      if (!keyword.trim() || keyword.length < 2) {
        setMovies([]);
        return;
      }

      try {
        setLoading(true);
        const response = await movieAPI.searchMovies({
          keyword: keyword.trim(),
          page: 0,
          size: 20,
        });
        const list = Array.isArray(response.data?.content) ? response.data.content : [];
        setMovies(list.map(normalizeMovie).filter((m) => m.tmdbId));
      } catch (error) {
        console.error('Failed to search movies:', error);
        setMovies([]);
      } finally {
        setLoading(false);
      }
    },
    []
  );

  // 디바운스된 검색
  useEffect(() => {
    if (!isOpen) return;

    const timer = setTimeout(() => {
      if (searchKeyword.trim().length >= 2) {
        handleSearch(searchKeyword);
      } else {
        setMovies([]);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchKeyword, isOpen, handleSearch]);

  const handleSelectMovie = (movie) => {
    setSelectedMovie(movie);
  };

  const handleConfirm = () => {
    if (selectedMovie) {
      onSelect(selectedMovie);
      onClose();
    }
  };

  const highlightText = (text, keyword) => {
    if (!keyword || !text) return text;
    const regex = new RegExp(`(${keyword})`, 'gi');
    const parts = text.split(regex);
    return parts.map((part, index) =>
      regex.test(part) ? (
        <span key={index} className="text-blue-600 font-semibold">
          {part}
        </span>
      ) : (
        part
      )
    );
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="relative w-full max-w-2xl max-h-[90vh] rounded-lg bg-white shadow-xl flex flex-col">
        {/* 헤더 */}
        <div className="flex items-center justify-between border-b border-gray-200 p-4">
          <h2 className="text-xl font-bold text-gray-900">영화 검색</h2>
          <button
            onClick={onClose}
            className="rounded-full p-1 text-gray-500 hover:bg-gray-100 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* 검색 입력 */}
        <div className="p-4 border-b border-gray-200">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
            <Input
              type="text"
              placeholder="영화 제목으로 검색..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        {/* 콘텐츠 영역 */}
        <div className="flex-1 overflow-y-auto p-4">
          {loading && (
            <div className="flex items-center justify-center py-12">
              <LoadingSpinner size="md" />
            </div>
          )}

          {!loading && selectedMovie && !searchKeyword && (
            <div className="mb-4">
              <p className="mb-2 text-sm font-medium text-gray-700">선택된 영화</p>
              <label
                htmlFor={`movie-search-${selectedMovie.tmdbId}`}
                className="block cursor-pointer rounded-lg bg-blue-50 p-4 ring-2 ring-blue-500 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <input
                    id={`movie-search-${selectedMovie.tmdbId}`}
                    type="radio"
                    name="movie-search"
                    value={selectedMovie.tmdbId}
                    checked
                    readOnly
                    className="h-5 w-5 accent-blue-600"
                  />
                  {selectedMovie.posterUrl && (
                    <div className="relative h-24 w-16 flex-shrink-0 overflow-hidden rounded">
                      <Image
                        src={getImageUrl(selectedMovie.posterUrl, 'w154')}
                        alt={selectedMovie.title}
                        fill
                        className="object-cover"
                      />
                    </div>
                  )}
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">{selectedMovie.title}</h3>
                    {selectedMovie.releaseDate && (
                      <p className="text-sm text-gray-500">
                        {new Date(selectedMovie.releaseDate).getFullYear()}
                      </p>
                    )}
                  </div>
                </div>
              </label>
            </div>
          )}

          {!loading && movies.length > 0 && (
            <div className="space-y-2">
              <p className="mb-2 text-sm font-medium text-gray-700">검색 결과</p>
              {movies.map((movie) => (
                <label
                  key={movie.tmdbId}
                  htmlFor={`movie-search-${movie.tmdbId}`}
                  className={`block cursor-pointer rounded-lg p-4 transition-colors ${
                    selectedMovie?.tmdbId === movie.tmdbId
                      ? 'bg-blue-50 ring-2 ring-blue-500'
                      : 'hover:bg-gray-50'
                  }`}
                >
                  <div className="flex items-center gap-4">
                    <input
                      id={`movie-search-${movie.tmdbId}`}
                      type="radio"
                      name="movie-search"
                      value={movie.tmdbId}
                      checked={selectedMovie?.tmdbId === movie.tmdbId}
                      onChange={() => handleSelectMovie(movie)}
                      className="h-5 w-5 accent-blue-600"
                    />
                    {movie.posterUrl && (
                      <div className="relative h-24 w-16 flex-shrink-0 overflow-hidden rounded">
                        <Image
                          src={getImageUrl(movie.posterUrl, 'w154')}
                          alt={movie.title}
                          fill
                          className="object-cover"
                        />
                      </div>
                    )}
                    <div className="flex-1">
                      <h3 className="font-semibold text-gray-900">
                        {highlightText(movie.title, searchKeyword)}
                      </h3>
                      {movie.releaseDate && (
                        <p className="text-sm text-gray-500">
                          {new Date(movie.releaseDate).getFullYear()}
                        </p>
                      )}
                    </div>
                  </div>
                </label>
              ))}
            </div>
          )}

          {!loading && searchKeyword && movies.length === 0 && (
            <div className="flex flex-col items-center justify-center py-12 text-gray-500">
              <Film className="mb-4 h-12 w-12 text-gray-400" />
              <p>검색 결과가 없습니다.</p>
            </div>
          )}

          {!loading && !searchKeyword && !selectedMovie && (
            <div className="flex flex-col items-center justify-center py-12 text-gray-500">
              <Search className="mb-4 h-12 w-12 text-gray-400" />
              <p>영화 제목을 검색해주세요.</p>
            </div>
          )}
        </div>

        {/* 푸터 */}
        <div className="flex items-center justify-end gap-2 border-t border-gray-200 p-4">
          <Button variant="outline" onClick={onClose}>
            취소
          </Button>
          <Button onClick={handleConfirm} disabled={!selectedMovie}>
            선택
          </Button>
        </div>
      </div>
    </div>
  );
}

