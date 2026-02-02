package org.example.backend.repository.review;

import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.review.Review;
import org.example.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByMovie(Movie movie, Pageable pageable);
    Page<Review> findByUser(User user, Pageable pageable);
    Page<Review> findByReviewIdIn(List<Long> reviewIds, Pageable pageable);

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Review> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<Review> findAllByOrderByLikeCountDesc(Pageable pageable);
    
    /** 키워드로 리뷰 목록 조회 (JOIN 쿼리로 성능 개선) */
    @Query("""
            SELECT DISTINCT r
            FROM Review r
            JOIN ReviewKeyword rk ON rk.review.reviewId = r.reviewId
            JOIN Keyword k ON k.keywordId = rk.keyword.keywordId
            WHERE k.name = :keyword
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByKeywordName(@Param("keyword") String keyword, Pageable pageable);
    
    /** tmdbId로 영화별 리뷰 목록 조회 */
    @Query("""
            SELECT r
            FROM Review r
            JOIN r.movie m
            WHERE m.tmdbId = :tmdbId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByMovieTmdbId(@Param("tmdbId") Long tmdbId, Pageable pageable);

    /** 영화 제목으로 리뷰 목록 조회 (사용자 입력용) */
    @Query("""
            SELECT r
            FROM Review r
            JOIN r.movie m
            WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :movieTitle, '%'))
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByMovieTitleContainsIgnoreCase(
            @Param("movieTitle") String movieTitle,
            Pageable pageable
    );
}
