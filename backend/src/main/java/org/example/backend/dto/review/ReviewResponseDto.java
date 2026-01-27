package org.example.backend.dto.review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponseDto(
        Long reviewId,
        String title,
        String content,
        int score,
        int viewCount,
        int likeCount,
        Boolean isLiked,
        ReviewAuthorDto author,
        ReviewMovieDto movie,
        List<String> keywords,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}


