package org.example.backend.dto.review;

public record ReviewLikeResponseDto(
        boolean isLiked,
        int likeCount
) {
}


