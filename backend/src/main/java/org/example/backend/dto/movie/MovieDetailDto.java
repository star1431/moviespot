package org.example.backend.dto.movie;

import java.time.LocalDateTime;

public record MovieDetailDto(
        Long id,
        String title,
        String releaseDate,
        String posterPath,
        String overview,
        Float voteAverage,
        LocalDateTime userRatingCreatedAt,
        Integer userScore,
        String userComment,
        Boolean isRecommended,
        Boolean isWatched,
        Integer watchedScore
) {
}

