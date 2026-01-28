package org.example.backend.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbMovieResponseDto(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("overview") String overview,
        @JsonProperty("vote_average") Float voteAverage,
        @JsonProperty("vote_count") Integer voteCount,
        @JsonProperty("runtime") Integer runtime,
        @JsonProperty("genre_ids") List<Long> genreIds
) {
}

