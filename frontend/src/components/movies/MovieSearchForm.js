'use client';

import { useState, useEffect } from 'react';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import Button from '@/components/ui/Button';
import { genreAPI } from '@/lib/api';

export default function MovieSearchForm({ onSearch, initialValues = {} }) {
  const [genres, setGenres] = useState([]);
  const [formData, setFormData] = useState({
    keyword: initialValues.keyword || '',
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

  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 50 }, (_, i) => currentYear - i);

  const handleChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const params = {};
    if (formData.keyword.trim()) params.keyword = formData.keyword.trim();
    if (formData.genreId) params.genreId = parseInt(formData.genreId);
    if (formData.releaseYearFrom) params.releaseYearFrom = parseInt(formData.releaseYearFrom);
    if (formData.releaseYearTo) params.releaseYearTo = parseInt(formData.releaseYearTo);
    if (formData.sortBy) params.sortBy = formData.sortBy;
    onSearch(params);
  };

  const handleReset = () => {
    setFormData({
      keyword: '',
      genreId: '',
      releaseYearFrom: '',
      releaseYearTo: '',
      sortBy: 'latest',
    });
    onSearch({});
  };

  return (
    <form onSubmit={handleSubmit} className="mb-6 rounded-lg bg-white p-6 shadow-sm">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-5">
        <Input
          label="제목 검색"
          placeholder="영화 제목을 입력하세요"
          value={formData.keyword}
          onChange={(e) => handleChange('keyword', e.target.value)}
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

