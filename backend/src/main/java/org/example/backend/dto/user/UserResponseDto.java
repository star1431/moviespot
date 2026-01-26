package org.example.backend.dto.user;

public record UserResponseDto(
        Long userId,
        String email,
        String nickname,
        String provider
) {
}

