package org.example.backend.dto.movie;

public record UserRatingRequestDto(
        int score,
        String comment,
        boolean isRecommended
) {
}

