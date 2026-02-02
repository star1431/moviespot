package org.example.backend.repository.movie;

import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.movie.MovieType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbId(Long tmdbId);
    /** 타입, 업데이트일자 >= 지정일 조회 */
    @Query("""
            SELECT COUNT(DISTINCT m)
            FROM Movie m JOIN m.movieTypes t
            WHERE t = :movieType AND m.updatedAt >= :dateTime
            """)
    long countByMovieTypeAndUpdatedAtGreaterThanEqual(
            @Param("movieType") MovieType movieType,
            @Param("dateTime") LocalDateTime dateTime
    );

    @Query("""
            SELECT DISTINCT m
            FROM Movie m JOIN m.movieTypes t
            WHERE t = :movieType AND m.updatedAt >= :dateTime
            ORDER BY m.createdAt DESC
            """)
    List<Movie> findByMovieTypeOrderByCreatedAtDesc(
            @Param("movieType") MovieType movieType,
            @Param("dateTime") LocalDateTime dateTime,
            Pageable pageable
    );

    /** 타입별 영화 목록 조회 */
    @Query("""
            SELECT DISTINCT m
            FROM Movie m JOIN m.movieTypes t
            WHERE t = :movieType
            """)
    List<Movie> findByMovieType(@Param("movieType") MovieType movieType);
    
    /** 영화 목록 조회 - 제목 검색, 개봉년도 필터, 정렬 */
    @Query("""
            SELECT DISTINCT m
            FROM Movie m
            WHERE (:keyword IS NULL OR LENGTH(:keyword) < 2 OR m.title LIKE CONCAT('%', :keyword, '%'))
            AND (:releaseYear IS NULL OR SUBSTRING(m.releaseDate, 1, 4) = STR(:releaseYear))
            ORDER BY 
                CASE WHEN :sortBy = 'rating' THEN m.tmdbRate END DESC NULLS LAST,
                CASE WHEN :sortBy = 'latest' THEN m.releaseDate END DESC NULLS LAST,
                CASE WHEN :sortBy = 'userRating' THEN 0 END,
                m.createdAt DESC
            """)
    List<Movie> findMoviesWithFilters(
            @Param("keyword") String keyword,
            @Param("releaseYear") Integer releaseYear,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );
    
    /** 우리회원평가순 정렬을 위한 쿼리 */
    @Query("""
            SELECT DISTINCT m
            FROM Movie m
            LEFT JOIN UserRating ur ON ur.movie.movieId = m.movieId
            WHERE (:keyword IS NULL OR LENGTH(:keyword) < 2 OR m.title LIKE CONCAT('%', :keyword, '%'))
            AND (:releaseYear IS NULL OR SUBSTRING(m.releaseDate, 1, 4) = STR(:releaseYear))
            GROUP BY m.movieId
            ORDER BY AVG(ur.score) DESC NULLS LAST, m.createdAt DESC
            """)
    List<Movie> findMoviesWithUserRatingSort(
            @Param("keyword") String keyword,
            @Param("releaseYear") Integer releaseYear,
            Pageable pageable
    );

    /** 리뷰 키워드로 영화 검색 (리뷰에 달린 keyword 기준) */
    @Query("""
            SELECT r.movie
            FROM ReviewKeyword rk
                JOIN rk.review r
                JOIN rk.keyword k
            WHERE k.name = :keyword
            GROUP BY r.movie.movieId
            ORDER BY MAX(r.createdAt) DESC
            """)
    List<Movie> findMoviesByReviewKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
