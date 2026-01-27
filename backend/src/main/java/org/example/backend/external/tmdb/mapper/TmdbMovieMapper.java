package org.example.backend.external.tmdb.mapper;

import org.example.backend.domain.movie.Movie;
import org.example.backend.external.tmdb.dto.TmdbMovieResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TmdbMovieMapper {

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    /** tmdb dto -> movie 변환 */
    public Movie toMovie(TmdbMovieResponseDto dto) {
        return Movie.builder()
                .tmdbId(dto.id())
                .title(dto.title())
                .releaseDate(dto.releaseDate())
                .posterUrl(dto.posterPath() != null
                        ? imageBaseUrl + dto.posterPath()
                        : null)
                .overview(dto.overview())
                .tmdbRate(dto.voteAverage())
                .build();
    }
}

