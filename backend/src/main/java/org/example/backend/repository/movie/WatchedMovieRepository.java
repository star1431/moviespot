package org.example.backend.repository.movie;

import org.example.backend.domain.movie.WatchedMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WatchedMovieRepository extends JpaRepository<WatchedMovie, Long> {
    Optional<WatchedMovie> findByUserUserIdAndMovieTmdbId(Long userId, Long tmdbId);
    void deleteByUserUserIdAndMovieTmdbId(Long userId, Long tmdbId);
    List<WatchedMovie> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
