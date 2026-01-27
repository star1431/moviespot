package org.example.backend.repository.review;

import org.example.backend.domain.review.ReviewLike;
import org.example.backend.domain.user.User;
import org.example.backend.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByReviewAndUser(Review review, User user);
    long countByReview(Review review);
    void deleteByReviewAndUser(Review review, User user);
    void deleteByReview(Review review);
}
