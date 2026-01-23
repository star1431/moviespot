package org.example.backend.repository.review;

import org.example.backend.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
