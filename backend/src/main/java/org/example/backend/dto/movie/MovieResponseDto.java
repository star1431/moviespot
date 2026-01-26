package org.example.backend.dto.movie;

public record MovieResponseDto(
        Long id,              // tmdbId
        String title,
        String releaseDate,
        String posterUrl,     // 전체 URL
        String overview,
        Float voteAverage
) {
}

