package org.example.backend.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.backend.domain.movie.Movie;

import java.util.List;

public record TmdbMovieResponseDto(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("overview") String overview,
        @JsonProperty("vote_average") Float voteAverage,
        @JsonProperty("genre_ids") List<Long> genreIds
) {
    public Movie toMovie(String imageBaseUrl) {
        return Movie.builder()
                .tmdbId(this.id)
                .title(this.title)
                .releaseDate(this.releaseDate)
                .posterUrl(this.posterPath != null 
                        ? imageBaseUrl + this.posterPath 
                        : null)
                .overview(this.overview)
                .tmdbRate(this.voteAverage)
                .build();
    }
    
    public static TmdbMovieResponseDto from(Movie movie, String imageBaseUrl) {
        String posterPath = movie.getPosterUrl() != null && movie.getPosterUrl().startsWith(imageBaseUrl)
                ? movie.getPosterUrl().substring(imageBaseUrl.length())
                : movie.getPosterUrl();
        
        return new TmdbMovieResponseDto(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getReleaseDate(),
                posterPath,
                movie.getOverview(),
                movie.getTmdbRate(),
                List.of() // genre_ids는 별도 조회 필요
        );
    }
}
