package org.example.backend.repository.movie;

import org.example.backend.domain.movie.WatchedMovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchedMovieRepository extends JpaRepository<WatchedMovie, Long> {
}
