package org.example.backend.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbVideoListResponseDto(
        @JsonProperty("id") Long id,
        @JsonProperty("results") List<TmdbVideoDto> results
) {
}


