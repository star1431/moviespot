package org.example.backend.dto.review;

public record ReviewMovieDto(
        Long tmdbId,
        String title,
        String posterUrl
) {
}


