package org.example.backend.dto.user;

import java.time.LocalDateTime;

public record AuthTokensDto(
        String accessToken,
        String refreshToken,
        LocalDateTime refreshExpiresAt
) {
}


