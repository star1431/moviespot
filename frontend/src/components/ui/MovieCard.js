import Link from 'next/link';
import Image from 'next/image';
import { getImageUrl, formatRating } from '@/lib/utils';
import { Star, Film } from 'lucide-react';

/**
 * 영화 카드 컴포넌트
 * @param {Object} movie - 영화 정보
 * @param {string} highlightKeyword - 하이라이트할 검색어 (선택)
 */
export default function MovieCard({ movie, highlightKeyword }) {
  if (!movie) return null;

  const { id, tmdbId, title, posterUrl, voteAverage, userAverageRating, releaseDate } = movie;
  const movieId = tmdbId || id;

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

  return (
    <Link href={`/movies/${movieId}`} className="group">
      <div className="relative overflow-hidden rounded-lg bg-white shadow-sm transition-all hover:shadow-lg">
        {/* 포스터 이미지 */}
        <div className="relative aspect-[2/3] w-full overflow-hidden bg-gray-200">
          {posterUrl ? (
            <Image
              src={getImageUrl(posterUrl)}
              alt={title}
              fill
              className="object-cover transition-transform group-hover:scale-105"
              sizes="(max-width: 768px) 50vw, (max-width: 1200px) 33vw, 20vw"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center bg-gray-200 text-gray-400">
              <Film className="h-12 w-12" />
            </div>
          )}
        </div>

        {/* 영화 정보 */}
        <div className="p-3">
          <h3 className="mb-2 line-clamp-2 text-sm font-semibold text-gray-900 group-hover:text-blue-600">
            {highlightKeyword ? highlightText(title, highlightKeyword) : title}
          </h3>
          
          <div className="flex items-center justify-between text-xs text-gray-600">
            {releaseDate && (
              <span>{new Date(releaseDate).getFullYear()}</span>
            )}
            <div className="flex items-center gap-1">
              <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
              <span className="font-medium">
                {formatRating(userAverageRating || voteAverage)}
              </span>
            </div>
          </div>
        </div>
      </div>
    </Link>
  );
}

