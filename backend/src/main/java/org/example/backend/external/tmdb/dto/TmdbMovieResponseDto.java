package org.example.backend.external.tmdb.dto;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieResponseDto(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("overview") String overview,
        @JsonProperty("vote_average") Float voteAverage,
        @JsonProperty("vote_count") Integer voteCount,
        @JsonProperty("runtime") Integer runtime,
        @JsonProperty("genre_ids") List<Long> genreIds,
        @JsonProperty("genres") List<TmdbGenreDto> genres
) {
        public List<Long> effectiveGenreIds() {
                if (genreIds != null && !genreIds.isEmpty()) {
                        return genreIds;
                }
                if (genres == null || genres.isEmpty()) {
                        return List.of();
                }
                return genres.stream()
                        .map(g -> g.id())
                        .filter(Objects::nonNull)
                        .toList();
        }
}

