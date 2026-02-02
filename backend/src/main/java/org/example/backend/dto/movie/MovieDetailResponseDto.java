package org.example.backend.dto.movie;

import org.example.backend.dto.common.SliceResponseDto;
import org.example.backend.dto.genre.GenreResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public record MovieDetailResponseDto(
        Long tmdbId,
        String title,
        String releaseDate,
        String posterUrl,
        String overview,
        Float voteAverage,        // tmdb 평점
        Integer voteCount,        // tmdb 투표 수
        Integer runtime,          // 플레이타임 (분 단위)
        List<Long> genreIds,      // 장르 ID 목록
        List<GenreResponseDto> genres, // tmdb genres 기반 (id,name)
        Double userAverageRating,  // 우리회원 평균 평점
        String trailerUrl,
        SliceResponseDto<MovieUserRatingResponseDto> userRatings,
        LocalDateTime myRatingCreatedAt,
        Integer myScore,
        String myComment,
        Boolean myRecommended,
        Boolean isWatched
) {
}



