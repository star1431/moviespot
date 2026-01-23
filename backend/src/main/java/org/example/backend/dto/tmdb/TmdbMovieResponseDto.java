package org.example.backend.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

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
}
