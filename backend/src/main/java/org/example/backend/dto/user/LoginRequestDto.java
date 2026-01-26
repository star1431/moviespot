package org.example.backend.dto.user;

public record LoginRequestDto(
        String email,
        String password
) {
}


