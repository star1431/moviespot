package org.example.backend.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbGenreResponseDto(
        @JsonProperty("genres") List<TmdbGenreDto> genres
) {
}
