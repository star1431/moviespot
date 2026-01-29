'use client';

import { useState, useEffect } from 'react';
import MovieGrid from '@/components/movies/MovieGrid';
import MovieSearchForm from '@/components/movies/MovieSearchForm';
import { movieAPI } from '@/lib/api';

const TABS = [
  { id: 'now-playing', label: '상영 중인 영화' },
  { id: 'top-rated', label: '전체 인기 영화' },
  { id: 'upcoming', label: '개봉 예정' },
  { id: 'search', label: '영화 찾기' },
];

export default function MoviesPage() {
  const [activeTab, setActiveTab] = useState('now-playing');
  const [searchParams, setSearchParams] = useState({});
  const [fetchFunction, setFetchFunction] = useState(null);

  useEffect(() => {
    const getFetchFunction = () => {
      switch (activeTab) {
        case 'now-playing':
          return (params) => {
            const { page = 0, size = 20, ...rest } = params;
            return movieAPI.getNowPlayingList(page, size);
          };
        case 'top-rated':
          return (params) => {
            const { page = 0, size = 20, ...rest } = params;
            return movieAPI.getTopRatedList(page, size);
          };
        case 'upcoming':
          return (params) => {
            const { page = 0, size = 20, ...rest } = params;
            return movieAPI.getUpcomingList(page, size);
          };
        case 'search':
          return (params) => movieAPI.searchMovies(params);
        default:
          return (params) => {
            const { page = 0, size = 20, ...rest } = params;
            return movieAPI.getNowPlayingList(page, size);
          };
      }
    };

    setFetchFunction(() => getFetchFunction());
  }, [activeTab]);

  const handleSearch = (params) => {
    setSearchParams(params);
    setActiveTab('search');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <h1 className="mb-6 text-3xl font-bold text-gray-900">영화 목록</h1>

        {/* 탭 */}
        <div className="mb-6 border-b border-gray-200">
          <div className="flex flex-wrap gap-2">
            {TABS.map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  setActiveTab(tab.id);
                  if (tab.id !== 'search') {
                    setSearchParams({});
                  }
                }}
                className={`px-4 py-2 text-sm font-medium transition-colors ${
                  activeTab === tab.id
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* 검색 폼 (영화 찾기 탭일 때만 표시) */}
        {activeTab === 'search' && (
          <MovieSearchForm
            onSearch={handleSearch}
            initialValues={searchParams}
          />
        )}

        {/* 영화 그리드 */}
        {fetchFunction && (
          <MovieGrid
            fetchFunction={fetchFunction}
            params={activeTab === 'search' ? searchParams : {}}
          />
        )}
      </div>
    </div>
  );
}

