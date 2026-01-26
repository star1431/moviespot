package org.example.backend.external.tmdb.mapper;

import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.movie.MovieType;
import org.example.backend.external.tmdb.dto.TmdbMovieResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TmdbMovieMapper {

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    /** TMDB DTO를 Movie 엔티티로 변환 (movieType 포함) */
    public Movie toMovie(TmdbMovieResponseDto dto, MovieType movieType) {
        return Movie.builder()
                .tmdbId(dto.id())
                .title(dto.title())
                .releaseDate(dto.releaseDate())
                .posterUrl(dto.posterPath() != null
                        ? imageBaseUrl + dto.posterPath()
                        : null)
                .overview(dto.overview())
                .tmdbRate(dto.voteAverage())
                .movieType(movieType)
                .build();
    }

    /** TMDB DTO를 Movie 엔티티로 변환 (movieType 없음 - 사용자가 저장한 영화용) */
    public Movie toMovie(TmdbMovieResponseDto dto) {
        return toMovie(dto, null);
    }
}

