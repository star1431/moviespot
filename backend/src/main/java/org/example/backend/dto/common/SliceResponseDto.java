package org.example.backend.dto.common;

import java.util.List;

public record SliceResponseDto<T>(
        List<T> content,
        int page,
        int size,
        boolean hasNext
) {
}


