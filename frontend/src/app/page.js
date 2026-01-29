'use client';

import MovieSlider from '@/components/movies/MovieSlider';
import { movieAPI } from '@/lib/api';
import { Film } from 'lucide-react';

export default function Home() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* 배너 섹션 */}
      <section className="relative bg-gradient-to-r from-blue-600 to-purple-600 py-20">
        <div className="container mx-auto px-4">
          <div className="mx-auto max-w-3xl text-center text-white">
            <div className="mb-6 flex justify-center">
              <Film className="h-16 w-16" />
            </div>
            <h1 className="mb-4 text-4xl font-bold md:text-5xl">
              MovieSpot에 오신 것을 환영합니다
            </h1>
            <p className="text-lg text-blue-100 md:text-xl">
              TMDB API를 활용한 최신 영화 정보와 함께
              <br />
              나만의 영화 컬렉션을 만들어보세요
            </p>
          </div>
        </div>
      </section>

      {/* 영화 섹션 */}
      <div className="container mx-auto px-4 py-12">
        <MovieSlider
          title="현재 상영 중"
          fetchFunction={movieAPI.getNowPlaying}
          className="mb-12"
        />
        <MovieSlider
          title="평점 높은 영화"
          fetchFunction={movieAPI.getTopRated}
        />
      </div>
    </div>
  );
}
