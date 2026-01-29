package org.example.backend.repository.genre;

import org.example.backend.domain.genre.MovieGenre;
import org.example.backend.domain.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    List<MovieGenre> findByMovie(Movie movie);
}
