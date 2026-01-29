'use client';

import { useState, useEffect } from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import MovieCard from '@/components/ui/MovieCard';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function MovieSlider({ title, fetchFunction, className = '' }) {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [swiperInstance, setSwiperInstance] = useState(null);
  const [isBeginning, setIsBeginning] = useState(true);
  const [isEnd, setIsEnd] = useState(false);

  useEffect(() => {
    const loadMovies = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await fetchFunction(0, 10);
        setMovies(response.data.content || []);
      } catch (error) {
        console.error('Failed to fetch movies:', error);
        console.error('Error details:', {
          message: error.message,
          code: error.code,
          response: error.response,
          config: error.config,
        });
        setMovies([]);
        setError('서버와 통신 실패했습니다');
      } finally {
        setLoading(false);
      }
    };

    loadMovies();
  }, [fetchFunction]);

  const handlePrev = () => {
    if (swiperInstance) {
      swiperInstance.slidePrev();
    }
  };

  const handleNext = () => {
    if (swiperInstance) {
      swiperInstance.slideNext();
    }
  };

  const handleSwiper = (swiper) => {
    setSwiperInstance(swiper);
    setIsBeginning(swiper.isBeginning);
    setIsEnd(swiper.isEnd);
  };

  const handleSlideChange = (swiper) => {
    setIsBeginning(swiper.isBeginning);
    setIsEnd(swiper.isEnd);
  };

  if (loading) {
    return (
      <section className={className}>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold text-gray-900">{title}</h2>
        </div>
        <div className="flex items-center justify-center py-12">
          <LoadingSpinner size="lg" />
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className={className}>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold text-gray-900">{title}</h2>
        </div>
        <div className="rounded-lg bg-red-50 p-4">
          <p className="text-center text-red-600 font-semibold">{error}</p>
        </div>
      </section>
    );
  }

  if (movies.length === 0) {
    return null;
  }

  const showNavigation = movies.length > 5;

  return (
    <section className={className}>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-xl font-bold text-gray-900">{title}</h2>
        {showNavigation && (
          <div className="flex gap-2">
            <button
              onClick={handlePrev}
              disabled={isBeginning}
              className="movie-slider-nav-btn"
              aria-label="이전"
            >
              <ChevronLeft className="h-5 w-5" />
            </button>
            <button
              onClick={handleNext}
              disabled={isEnd}
              className="movie-slider-nav-btn"
              aria-label="다음"
            >
              <ChevronRight className="h-5 w-5" />
            </button>
          </div>
        )}
      </div>
      <Swiper
        spaceBetween={16}
        slidesPerView={2}
        slidesPerGroup={2}
        onSwiper={handleSwiper}
        onSlideChange={handleSlideChange}
        breakpoints={{
          640: {
            slidesPerView: 3,
            slidesPerGroup: 3,
          },
          768: {
            slidesPerView: 4,
            slidesPerGroup: 4,
          },
          1024: {
            slidesPerView: 5,
            slidesPerGroup: 5,
          },
        }}
        className="movie-swiper"
      >
        {movies.map((movie, index) => (
          <SwiperSlide key={movie.id || movie.tmdbId || index}>
            <MovieCard movie={movie} />
          </SwiperSlide>
        ))}
      </Swiper>
    </section>
  );
}
