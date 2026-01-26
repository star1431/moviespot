package org.example.backend.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbMovieListResponseDto(
        @JsonProperty("page") Integer page,
        @JsonProperty("results") List<TmdbMovieResponseDto> results,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("total_results") Integer totalResults
) {
}

