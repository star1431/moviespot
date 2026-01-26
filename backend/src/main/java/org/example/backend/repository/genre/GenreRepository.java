package org.example.backend.repository.genre;

import org.example.backend.domain.genre.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
