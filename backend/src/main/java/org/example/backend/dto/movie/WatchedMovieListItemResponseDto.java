package org.example.backend.dto.movie;

import java.time.LocalDateTime;

public record WatchedMovieListItemResponseDto(
        Long tmdbId,
        String title,
        String posterUrl,
        int score,
        LocalDateTime createdAt
) {
}



