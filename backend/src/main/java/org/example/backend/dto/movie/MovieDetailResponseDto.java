package org.example.backend.dto.movie;

import org.example.backend.dto.common.SliceResponseDto;

import java.time.LocalDateTime;

public record MovieDetailResponseDto(
        Long tmdbId,
        String title,
        String releaseDate,
        String posterPath,
        String overview,
        Float voteAverage,
        SliceResponseDto<MovieUserRatingResponseDto> userRatings,
        LocalDateTime myRatingCreatedAt,
        Integer myScore,
        String myComment,
        Boolean myRecommended,
        Boolean isWatched,
        Integer myWatchedScore
) {
}


