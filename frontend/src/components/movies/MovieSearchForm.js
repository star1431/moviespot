'use client';

import Link from 'next/link';
import { useState, useEffect } from 'react';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import Button from '@/components/ui/Button';
import { genreAPI, userAPI } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';

export default function MovieSearchForm({ onSearch, initialValues = {} }) {
  const { isAuthenticated } = useAuth();
  const [genres, setGenres] = useState([]);
  const [myGenres, setMyGenres] = useState([]);
  const [myKeywords, setMyKeywords] = useState([]);
  const [formData, setFormData] = useState({
    // 제목 검색용 인풋은 "titleKeyword"만 바인딩한다.
    titleKeyword:
      (initialValues.keywordType || 'title') === 'review' ? '' : (initialValues.keyword || ''),
    // 관심 키워드(리뷰 키워드) 선택은 별도 상태로 관리한다.
    reviewKeyword:
      (initialValues.keywordType || 'title') === 'review' ? (initialValues.keyword || '') : '',
    keywordType: initialValues.keywordType || 'title',
    genreId: initialValues.genreId || '',
    releaseYearFrom: initialValues.releaseYearFrom || '',
    releaseYearTo: initialValues.releaseYearTo || '',
    sortBy: initialValues.sortBy || 'latest',
  });

  useEffect(() => {
    const loadGenres = async () => {
      try {
        const response = await genreAPI.getGenres();
        setGenres(response.data || []);
      } catch (error) {
        console.error('Failed to fetch genres:', error);
      }
    };

    loadGenres();
  }, []);

  useEffect(() => {
    const loadMyInterests = async () => {
      if (!isAuthenticated) {
        setMyGenres([]);
        setMyKeywords([]);
        return;
      }
      try {
        const [myGenresRes, myKeywordsRes] = await Promise.all([
          userAPI.getGenres(),
          userAPI.getKeywords(),
        ]);
        setMyGenres(Array.isArray(myGenresRes.data) ? myGenresRes.data : []);
        setMyKeywords(Array.isArray(myKeywordsRes.data) ? myKeywordsRes.data : []);
      } catch (error) {
        // 관심 정보 조회 실패는 검색 기능 자체를 막지 않음
        setMyGenres([]);
        setMyKeywords([]);
      }
    };

    loadMyInterests();
  }, [isAuthenticated]);

  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 50 }, (_, i) => currentYear - i);

  const handleChange = (field, value) => {
    setFormData((prev) => {
      if (field === 'titleKeyword') {
        // 제목 인풋에 타이핑하면 제목검색 모드로 전환하고 리뷰키워드 선택은 해제
        return { ...prev, titleKeyword: value, keywordType: 'title', reviewKeyword: '' };
      }
      return { ...prev, [field]: value };
    });
  };

  const submitWith = (nextFormData) => {
    const params = {};
    const keywordType = nextFormData.keywordType || 'title';
    const keywordValue =
      keywordType === 'review' ? (nextFormData.reviewKeyword || '') : (nextFormData.titleKeyword || '');
    if (keywordValue.trim()) {
      params.keyword = keywordValue.trim();
      params.keywordType = keywordType;
    }
    if (nextFormData.genreId) params.genreId = parseInt(nextFormData.genreId);
    if (nextFormData.releaseYearFrom) params.releaseYearFrom = parseInt(nextFormData.releaseYearFrom);
    if (nextFormData.releaseYearTo) params.releaseYearTo = parseInt(nextFormData.releaseYearTo);
    if (nextFormData.sortBy) params.sortBy = nextFormData.sortBy;
    onSearch(params);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    submitWith(formData);
  };

  const handleReset = () => {
    const next = {
      titleKeyword: '',
      reviewKeyword: '',
      keywordType: 'title',
      genreId: '',
      releaseYearFrom: '',
      releaseYearTo: '',
      sortBy: 'latest',
    };
    setFormData(next);
    onSearch({});
  };

  const normalizedMyGenres = Array.isArray(myGenres)
    ? myGenres
        .map((g) => ({
          id: g?.id ?? g?.genreId,
          name: g?.name,
        }))
        .filter((g) => g.id && g.name)
    : [];

  const normalizedMyKeywords = Array.isArray(myKeywords)
    ? myKeywords
        .map((k) => (typeof k === 'string' ? k : k?.name ?? k?.keywordName ?? ''))
        .map((s) => (s || '').trim())
        .filter((s) => s.length >= 1)
    : [];

  return (
    <form onSubmit={handleSubmit} className="mb-6 rounded-lg bg-white p-6 shadow-sm">
      {/* 로그인 사용자: 관심 장르/키워드 빠른 검색 */}
      {isAuthenticated && (
        <div className="mb-5 rounded-lg border border-gray-200 bg-white p-4">
          <div className="mb-3 text-sm font-semibold text-gray-900">내 관심으로 빠른 검색</div>

          {normalizedMyGenres.length > 0 && (
            <div className="mb-3">
              <div className="mb-2 text-xs font-medium text-gray-600">관심 장르</div>
              <div className="flex flex-wrap gap-2">
                {normalizedMyGenres.slice(0, 10).map((g) => (
                  <button
                    key={g.id}
                    type="button"
                    onClick={() => {
                      // 장르로 빠른검색: discover 기반으로 쓰기 위해 keyword는 비움
                      const next = {
                        ...formData,
                        keyword: '',
                        keywordType: 'title',
                        genreId: String(g.id),
                      };
                      setFormData(next);
                      submitWith(next);
                    }}
                    className={`rounded-full border px-3 py-1 text-sm transition-colors ${
                      String(formData.genreId) === String(g.id)
                        ? 'border-blue-300 bg-blue-50 text-blue-700'
                        : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    {g.name}
                  </button>
                ))}
              </div>
            </div>
          )}
          {normalizedMyGenres.length === 0 && (
            <div className="mb-3 text-sm text-gray-600">
              현재 관심 장르가 없습니다.{' '}
              <Link href="/collection" className="font-semibold text-blue-600 hover:underline">
                나의컬렉션
              </Link>
              에서 설정하세요.
            </div>
          )}

          {normalizedMyKeywords.length > 0 && (
            <div>
              <div className="mb-2 text-xs font-medium text-gray-600">관심 키워드</div>
              <div className="flex flex-wrap gap-2">
                {normalizedMyKeywords.slice(0, 10).map((kw) => (
                  <button
                    key={kw}
                    type="button"
                    onClick={() => {
                      // 관심 키워드는 "리뷰 키워드" 검색
                      const next = {
                        ...formData,
                        genreId: '',
                        reviewKeyword: kw,
                        keywordType: 'review',
                      };
                      setFormData(next);
                      submitWith(next);
                    }}
                    className={`rounded-full border px-3 py-1 text-sm transition-colors ${
                      (formData.reviewKeyword || '').trim() === kw && formData.keywordType === 'review'
                        ? 'border-purple-300 bg-purple-50 text-purple-700'
                        : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    {kw}
                  </button>
                ))}
              </div>
            </div>
          )}
          {normalizedMyKeywords.length === 0 && (
            <div className="text-sm text-gray-600">
              현재 관심 키워드가 없습니다.{' '}
              <Link href="/collection" className="font-semibold text-blue-600 hover:underline">
                나의컬렉션
              </Link>
              에서 설정하세요.
            </div>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-5">
        {/* 리뷰 키워드 선택 상태 표시 (제목 인풋에 값 주입하지 않음) */}
        {isAuthenticated && formData.keywordType === 'review' && (formData.reviewKeyword || '').trim() && (
          <div className="md:col-span-2 lg:col-span-5">
            <div className="flex flex-wrap items-center gap-2 rounded-lg border border-purple-200 bg-purple-50 px-3 py-2 text-sm text-purple-800">
              <span className="font-medium">리뷰 키워드</span>
              <span className="rounded-full bg-white px-3 py-1 text-purple-700 border border-purple-200">
                {formData.reviewKeyword}
              </span>
              <button
                type="button"
                onClick={() => {
                  const next = { ...formData, reviewKeyword: '', keywordType: 'title' };
                  setFormData(next);
                  submitWith(next);
                }}
                className="ml-auto text-purple-700 hover:underline"
              >
                해제
              </button>
            </div>
          </div>
        )}
        <Input
          label="제목 검색"
          placeholder="영화 제목을 입력하세요"
          value={formData.titleKeyword}
          onChange={(e) => handleChange('titleKeyword', e.target.value)}
        />
        <Select
          label="장르"
          value={formData.genreId}
          onChange={(e) => handleChange('genreId', e.target.value)}
          options={[
            { value: '', label: '전체' },
            ...genres.map((genre) => ({
              value: genre.id.toString(),
              label: genre.name,
            })),
          ]}
        />
        <Select
          label="시작 연도"
          value={formData.releaseYearFrom}
          onChange={(e) => handleChange('releaseYearFrom', e.target.value)}
          options={[
            { value: '', label: '전체' },
            ...years.map((year) => ({
              value: year.toString(),
              label: year.toString(),
            })),
          ]}
        />
        <Select
          label="종료 연도"
          value={formData.releaseYearTo}
          onChange={(e) => handleChange('releaseYearTo', e.target.value)}
          options={[
            { value: '', label: '전체' },
            ...years.map((year) => ({
              value: year.toString(),
              label: year.toString(),
            })),
          ]}
        />
        <div className="flex items-end">
          <div className="flex w-full gap-2">
            <Button type="submit" className="flex-1">
              검색
            </Button>
            <Button type="button" variant="secondary" onClick={handleReset}>
              초기화
            </Button>
          </div>
        </div>
      </div>
    </form>
  );
}

