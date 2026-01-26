package org.example.backend.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbGenreResponseDto(
        @JsonProperty("genres") List<TmdbGenreDto> genres
) {
}

