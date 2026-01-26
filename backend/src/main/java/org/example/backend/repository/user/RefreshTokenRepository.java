package org.example.backend.repository.user;

import org.example.backend.domain.user.RefreshToken;
import org.example.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByToken(String token);
    void deleteByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime now);
}

