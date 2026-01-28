package org.example.backend.dto.movie;

public record MovieResponseDto(
        Long id,              // tmdbId
        String title,
        String releaseDate,
        String posterUrl,     // 전체 URL
        Float voteAverage,    // tmdb 평점
        Double userAverageRating  // 우리회원 평균 평점
) {
}

