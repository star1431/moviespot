package org.example.backend.repository.review;

import java.util.List;
import java.util.Optional;

import org.example.backend.domain.movie.UserRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
    Optional<UserRating> findByUserUserIdAndMovieTmdbId(Long userId, Long tmdbId);
    void deleteByUserUserIdAndMovieTmdbId(Long userId, Long tmdbId);
    List<UserRating> findByMovieTmdbIdOrderByCreatedAtDesc(Long tmdbId, Pageable pageable);
    
    /** 특정 영화의 평균 평점 계산 */
    @Query("SELECT AVG(ur.score) FROM UserRating ur WHERE ur.movie.tmdbId = :tmdbId")
    Optional<Double> calculateAverageRatingByTmdbId(@Param("tmdbId") Long tmdbId);
    
    /** 여러 영화의 평균 평점을 한 번에 조회 */
    @Query("""
            SELECT ur.movie.tmdbId, AVG(ur.score) as avgRating
            FROM UserRating ur
            WHERE ur.movie.tmdbId IN :tmdbIds
            GROUP BY ur.movie.tmdbId
            """)
    List<Object[]> calculateAverageRatingsByTmdbIds(@Param("tmdbIds") List<Long> tmdbIds);
}
