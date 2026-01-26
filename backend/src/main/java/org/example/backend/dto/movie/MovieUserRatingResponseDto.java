package org.example.backend.dto.movie;

import java.time.LocalDateTime;

public record MovieUserRatingResponseDto(
        Long userId,
        String nickname,
        int score,
        String comment,
        boolean isRecommended,
        LocalDateTime createdAt
) {
}


