package org.example.backend.repository.review;

import org.example.backend.domain.movie.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
}
