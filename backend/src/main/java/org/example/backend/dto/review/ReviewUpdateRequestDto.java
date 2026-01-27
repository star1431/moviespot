package org.example.backend.dto.review;

import java.util.List;

public record ReviewUpdateRequestDto(
        String title,
        String content,
        int score,
        List<String> keywords
) {
}


