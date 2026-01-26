package org.example.backend.repository.genre;

import org.example.backend.domain.genre.UserGenre;
import org.example.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    List<UserGenre> findByUser(User user);
    Optional<UserGenre> findByUserUserIdAndGenreTmdbGenreId(Long userId, Long tmdbGenreId);
    void deleteByUserUserIdAndGenreTmdbGenreId(Long userId, Long tmdbGenreId);
}
