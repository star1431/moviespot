package org.example.backend.repository.keyword;

import org.example.backend.domain.keyword.ReviewKeyword;
import org.example.backend.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Long> {
    List<ReviewKeyword> findByReview(Review review);
    void deleteByReview(Review review);
    List<ReviewKeyword> findByKeywordName(String name);
}
