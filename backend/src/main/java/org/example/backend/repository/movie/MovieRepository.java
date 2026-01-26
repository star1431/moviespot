package org.example.backend.repository.movie;

import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.movie.MovieType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbId(Long tmdbId);
    
    List<Movie> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    long countByUpdatedAtAfter(LocalDateTime dateTime);
    
    List<Movie> findByMovieTypeOrderByCreatedAtDesc(MovieType movieType, Pageable pageable);
    
    long countByMovieTypeAndUpdatedAtAfter(MovieType movieType, LocalDateTime dateTime);
    
    List<Movie> findByMovieType(MovieType movieType);
}
