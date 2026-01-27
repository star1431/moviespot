package org.example.backend.dto.movie;

import org.example.backend.dto.common.SliceResponseDto;

import java.time.LocalDateTime;

public record MovieDetailResponseDto(
        Long tmdbId,
        String title,
        String releaseDate,
        String posterUrl,
        String overview,
        Float voteAverage,        // TMDB 평점
        Double userAverageRating,  // 우리회원 평균 평점
        String trailerUrl,
        SliceResponseDto<MovieUserRatingResponseDto> userRatings,
        LocalDateTime myRatingCreatedAt,
        Integer myScore,
        String myComment,
        Boolean myRecommended,
        Boolean isWatched,
        Integer myWatchedScore
) {
}



