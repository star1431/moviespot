package org.example.backend.repository.review;

import org.example.backend.domain.movie.UserRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
    Optional<UserRating> findByUserUserIdAndMovieTmdbId(Long userId, Long tmdbId);
    void deleteByUserUserIdAndMovieTmdbId(Long userId, Long tmdbId);
    List<UserRating> findByMovieTmdbIdOrderByCreatedAtDesc(Long tmdbId, Pageable pageable);
}
