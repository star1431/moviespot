package org.example.backend.repository.genre;

import org.example.backend.domain.genre.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
}
