package org.example.backend.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbGenreResponseDto(
        @JsonProperty("genres") List<Genre> genres
) {
    public record Genre(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {
    }
}
