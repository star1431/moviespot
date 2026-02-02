import Image from 'next/image';
import { Film } from 'lucide-react';
import { getImageUrl } from '@/lib/utils';

export default function SelectedMovieInfo({ title, releaseDate, posterUrl }) {
  const year = releaseDate ? new Date(releaseDate).getFullYear() : null;

  return (
    <div className="flex items-center gap-4">
      <div className="relative h-16 w-12 flex-shrink-0 overflow-hidden rounded bg-gray-100">
        {posterUrl ? (
          <Image
            src={getImageUrl(posterUrl, 'w154')}
            alt={title || '영화 포스터'}
            fill
            className="object-cover"
            sizes="48px"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center">
            <Film className="h-6 w-6 text-gray-400" />
          </div>
        )}
      </div>

      <div className="min-w-0">
        <div className="truncate font-medium text-gray-900">{title || '선택된 영화'}</div>
        {year != null && <div className="text-sm text-gray-500">{year}</div>}
      </div>
    </div>
  );
}


