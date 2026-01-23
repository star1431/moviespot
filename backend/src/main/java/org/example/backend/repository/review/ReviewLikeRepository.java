package org.example.backend.repository.review;

import org.example.backend.domain.review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
}
