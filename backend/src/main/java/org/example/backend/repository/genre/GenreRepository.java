package org.example.backend.repository.genre;

import org.example.backend.domain.genre.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByTmdbGenreId(Long tmdbGenreId);
}
