'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { reviewAPI, movieAPI, genreAPI } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { Film, Star, X } from 'lucide-react';
import Link from 'next/link';

export default function CreateReviewPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const tmdbId = searchParams.get('tmdbId');

  const [formData, setFormData] = useState({
    tmdbId: tmdbId ? parseInt(tmdbId) : null,
    title: '',
    content: '',
    score: 5,
    keywords: [],
  });
  const [newKeyword, setNewKeyword] = useState('');
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError('');
  };

  const handleScoreChange = (score) => {
    setFormData({ ...formData, score });
  };

  const handleAddKeyword = () => {
    if (newKeyword.trim() && !formData.keywords.includes(newKeyword.trim())) {
      setFormData({
        ...formData,
        keywords: [...formData.keywords, newKeyword.trim()],
      });
      setNewKeyword('');
    }
  };

  const handleRemoveKeyword = (keyword) => {
    setFormData({
      ...formData,
      keywords: formData.keywords.filter((k) => k !== keyword),
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.tmdbId) {
      setError('영화를 선택해주세요.');
      return;
    }

    if (!formData.title.trim()) {
      setError('제목을 입력해주세요.');
      return;
    }

    if (!formData.content.trim()) {
      setError('내용을 입력해주세요.');
      return;
    }

    setLoading(true);

    try {
      await reviewAPI.create({
        tmdbId: formData.tmdbId,
        title: formData.title.trim(),
        content: formData.content.trim(),
        score: formData.score,
        keywords: formData.keywords,
      });

      router.push('/reviews');
    } catch (error) {
      console.error('Failed to create review:', error);
      setError(
        error.response?.data?.error || '리뷰 작성에 실패했습니다. 다시 시도해주세요.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleMovieSearch = async (keyword) => {
    if (!keyword.trim() || keyword.length < 2) {
      setMovies([]);
      return;
    }

    try {
      const response = await movieAPI.searchMovies({
        keyword: keyword.trim(),
        page: 0,
        size: 5,
      });
      setMovies(response.data.content || []);
    } catch (error) {
      console.error('Failed to search movies:', error);
      setMovies([]);
    }
  };

  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <div className="mx-auto max-w-3xl">
          <h1 className="mb-6 text-3xl font-bold text-gray-900">리뷰 작성</h1>

          {error && (
            <div className="mb-4 rounded-lg bg-red-50 p-4 text-sm text-red-800">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 영화 선택 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                영화 선택 *
              </label>
              {formData.tmdbId ? (
                <div className="flex items-center justify-between rounded-lg border border-gray-300 bg-white p-4">
                  <span className="text-gray-900">
                    선택된 영화 ID: {formData.tmdbId}
                  </span>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setFormData({ ...formData, tmdbId: null })}
                  >
                    변경
                  </Button>
                </div>
              ) : (
                <div>
                  <Input
                    type="text"
                    placeholder="영화 제목으로 검색..."
                    onChange={(e) => handleMovieSearch(e.target.value)}
                    className="mb-2"
                  />
                  {movies.length > 0 && (
                    <div className="max-h-48 overflow-y-auto rounded-lg border border-gray-300 bg-white">
                      {movies.map((movie) => (
                        <button
                          key={movie.tmdbId}
                          type="button"
                          onClick={() =>
                            setFormData({ ...formData, tmdbId: movie.tmdbId })
                          }
                          className="w-full px-4 py-2 text-left hover:bg-gray-50"
                        >
                          <div className="font-medium">{movie.title}</div>
                          <div className="text-sm text-gray-500">
                            {movie.releaseDate}
                          </div>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 제목 */}
            <Input
              label="제목 *"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              placeholder="리뷰 제목을 입력하세요"
              required
            />

            {/* 평점 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                평점 * ({formData.score}점)
              </label>
              <div className="flex gap-2">
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((score) => (
                  <button
                    key={score}
                    type="button"
                    onClick={() => handleScoreChange(score)}
                    className={`flex h-10 w-10 items-center justify-center rounded-lg border transition-colors ${
                      formData.score === score
                        ? 'border-blue-500 bg-blue-50 text-blue-600'
                        : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    <Star
                      className={`h-5 w-5 ${
                        formData.score >= score ? 'fill-yellow-400 text-yellow-400' : ''
                      }`}
                    />
                  </button>
                ))}
              </div>
            </div>

            {/* 내용 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                내용 *
              </label>
              <textarea
                name="content"
                value={formData.content}
                onChange={handleInputChange}
                placeholder="리뷰 내용을 입력하세요"
                rows={10}
                className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                required
              />
            </div>

            {/* 키워드 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                키워드
              </label>
              <div className="mb-2 flex flex-wrap gap-2">
                {formData.keywords.map((keyword) => (
                  <span
                    key={keyword}
                    className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-3 py-1 text-sm text-blue-800"
                  >
                    {keyword}
                    <button
                      type="button"
                      onClick={() => handleRemoveKeyword(keyword)}
                      className="hover:text-blue-900"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </span>
                ))}
              </div>
              <div className="flex gap-2">
                <Input
                  type="text"
                  value={newKeyword}
                  onChange={(e) => setNewKeyword(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddKeyword())}
                  placeholder="키워드 입력 후 Enter"
                  className="flex-1"
                />
                <Button type="button" onClick={handleAddKeyword}>
                  추가
                </Button>
              </div>
            </div>

            {/* 버튼 */}
            <div className="flex gap-4">
              <Button type="submit" disabled={loading} className="flex-1">
                {loading ? <LoadingSpinner size="sm" /> : '리뷰 작성'}
              </Button>
              <Link href="/reviews">
                <Button type="button" variant="outline">
                  취소
                </Button>
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

