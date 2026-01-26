package org.example.backend.dto.movie;

public record MovieUserRatingUpsertRequestDto(
        Long tmdbId,
        int score,
        String comment,
        boolean isRecommended
) {
}


