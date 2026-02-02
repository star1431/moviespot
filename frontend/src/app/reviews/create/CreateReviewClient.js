'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { movieAPI, reviewAPI } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import MovieSearchModal from '@/components/movies/MovieSearchModal';
import { Star, X, Film } from 'lucide-react';
import Link from 'next/link';
import SelectedMovieInfo from '@/components/movies/SelectedMovieInfo';

export default function CreateReviewClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const tmdbId = searchParams.get('tmdbId');

  const [formData, setFormData] = useState({
    tmdbId: tmdbId ? parseInt(tmdbId) : null,
    movieTitle: tmdbId ? null : null, // 영화 제목 저장
    moviePosterUrl: null,
    movieReleaseDate: null,
    title: '',
    content: '',
    score: 5,
    keywords: [],
  });
  const [newKeyword, setNewKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showMovieModal, setShowMovieModal] = useState(false);

  // 영화상세에서 tmdbId로 진입한 경우, 선택된 영화 정보를 자동으로 채운다.
  useEffect(() => {
    const id = tmdbId ? parseInt(tmdbId) : null;
    if (!id) return;
    if (formData.movieTitle && formData.moviePosterUrl) return;

    const load = async () => {
      try {
        const res = await movieAPI.getMovieDetail(id, 0, 1);
        setFormData((prev) => ({
          ...prev,
          tmdbId: id,
          movieTitle: res.data?.title ?? prev.movieTitle,
          moviePosterUrl: res.data?.posterUrl ?? prev.moviePosterUrl,
          movieReleaseDate: res.data?.releaseDate ?? prev.movieReleaseDate,
        }));
      } catch (e) {
        // 영화 정보 로드 실패 시에도 리뷰 작성은 가능(사용자가 모달로 재선택)
      }
    };

    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tmdbId]);

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
    } catch (err) {
      console.error('Failed to create review:', err);
      setError(err.response?.data?.error || '리뷰 작성에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const handleMovieSelect = (movie) => {
    setFormData({
      ...formData,
      tmdbId: movie.tmdbId,
      movieTitle: movie.title,
      moviePosterUrl: movie.posterUrl,
      movieReleaseDate: movie.releaseDate ?? null,
    });
  };

  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="mx-auto max-w-3xl">
            <div className="rounded-lg bg-white p-8 text-center shadow-sm">
              <Film className="mx-auto mb-4 h-16 w-16 text-gray-400" />
              <h2 className="mb-2 text-2xl font-bold text-gray-900">로그인이 필요한 서비스입니다</h2>
              <p className="mb-6 text-gray-600">리뷰를 작성하려면 로그인해주세요</p>
              <Link href="/login">
                <Button size="lg">로그인하기</Button>
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
        <div className="mx-auto max-w-3xl">
          <h1 className="mb-6 text-3xl font-bold text-gray-900">리뷰 작성</h1>

          {error && (
            <div className="mb-4 rounded-lg bg-red-50 p-4 text-sm text-red-800">{error}</div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 영화 선택 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">영화 선택 *</label>
              {formData.tmdbId ? (
                <div className="flex items-center justify-between rounded-lg border border-gray-300 bg-white p-4">
                  <SelectedMovieInfo
                    title={formData.movieTitle || `영화 ID: ${formData.tmdbId}`}
                    releaseDate={formData.movieReleaseDate}
                    posterUrl={formData.moviePosterUrl}
                  />
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() =>
                      setFormData({
                        ...formData,
                        tmdbId: null,
                        movieTitle: null,
                        moviePosterUrl: null,
                        movieReleaseDate: null,
                      })
                    }
                  >
                    변경
                  </Button>
                </div>
              ) : (
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowMovieModal(true)}
                  className="w-full"
                >
                  <Film className="mr-2 h-4 w-4" />
                  영화 검색
                </Button>
              )}
            </div>

            {/* 제목 */}
            <Input
              label="제목 *"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              placeholder="리뷰 제목을 입력하세요"
              className="bg-white"
              required
            />

            {/* 평점 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                평점 * ({formData.score}점)
              </label>
              <div className="flex flex-wrap items-center bg-white rounded-lg border border-gray-300">
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((score, idx) => (
                  <button
                    key={score}
                    type="button"
                    onClick={() => handleScoreChange(score)}
                    className="group flex h-10 w-10 items-center justify-center bg-transparent p-0 transition-transform hover:scale-110 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/30"
                  >
                    <Star
                      className={`h-5 w-5 ${
                        formData.score >= score
                          ? 'fill-yellow-400 text-yellow-400'
                          : 'fill-transparent text-gray-500'
                      }`}
                    />
                  </button>
                ))}
              </div>
            </div>

            {/* 내용 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">내용 *</label>
              <textarea
                name="content"
                value={formData.content}
                onChange={handleInputChange}
                placeholder="리뷰 내용을 입력하세요"
                rows={10}
                className="w-full rounded-lg border border-gray-300 bg-white px-4 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                required
              />
            </div>

            {/* 키워드 */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">키워드</label>
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
                  className="flex-1 min-w-0 bg-white"
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleAddKeyword}
                  className="whitespace-nowrap px-4"
                >
                  +추가
                </Button>
              </div>
            </div>

            {/* 버튼 */}
            <div className="flex gap-4">
              <Link href="/reviews" className="flex-1">
                <Button type="button" variant="outline" className="w-full">
                  취소
                </Button>
              </Link>
              <Button type="submit" disabled={loading} className="flex-1">
                {loading ? <LoadingSpinner size="sm" /> : '리뷰 작성'}
              </Button>
            </div>
          </form>

          {/* 영화 검색 모달 */}
          <MovieSearchModal
            isOpen={showMovieModal}
            onClose={() => setShowMovieModal(false)}
            onSelect={handleMovieSelect}
            initialTmdbId={tmdbId ? parseInt(tmdbId) : null}
          />
        </div>
      </div>
    </div>
  );
}


