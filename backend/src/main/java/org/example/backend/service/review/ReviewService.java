package org.example.backend.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.keyword.Keyword;
import org.example.backend.domain.keyword.ReviewKeyword;
import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.review.ReviewLike;
import org.example.backend.domain.review.Review;
import org.example.backend.domain.user.User;
import org.example.backend.dto.review.ReviewAuthorDto;
import org.example.backend.dto.review.ReviewCreateRequestDto;
import org.example.backend.dto.review.ReviewLikeResponseDto;
import org.example.backend.dto.review.ReviewMovieDto;
import org.example.backend.dto.review.ReviewResponseDto;
import org.example.backend.dto.review.ReviewUpdateRequestDto;
import org.example.backend.repository.keyword.ReviewKeywordRepository;
import org.example.backend.repository.movie.MovieRepository;
import org.example.backend.repository.review.ReviewLikeRepository;
import org.example.backend.repository.review.ReviewRepository;
import org.example.backend.repository.user.UserRepository;
import org.example.backend.service.keyword.KeywordService;
import org.example.backend.service.movie.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final KeywordService keywordService;
    private final MovieService movieService;

    /** 리뷰 작성 */
    public ReviewResponseDto createReview(Long userId, ReviewCreateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        // tmdbId로 영화 조회, 없으면 TMDB API에서 가져와서 자동 생성
        Movie movie = movieService.getOrCreateMovieByTmdbId(requestDto.tmdbId());

        Review review = Review.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .score(requestDto.score())
                .user(user)
                .movie(movie)
                .viewCount(0)
                .likeCount(0)
                .build();

        Review savedReview = reviewRepository.save(review);
        updateReviewKeywords(savedReview, requestDto.keywords());
        log.info("리뷰 작성 완료: reviewId={}, userId={}, tmdbId={}", savedReview.getReviewId(), userId, requestDto.tmdbId());
        return toResponseDto(savedReview, false, userId);
    }

    /** 리뷰 수정 */
    public ReviewResponseDto updateReview(Long reviewId, Long userId, ReviewUpdateRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("리뷰 수정 권한이 없습니다.");
        }

        Review updatedReview = review.toBuilder()
                .title(requestDto.title())
                .content(requestDto.content())
                .score(requestDto.score())
                .build();

        Review savedReview = reviewRepository.save(updatedReview);
        updateReviewKeywords(savedReview, requestDto.keywords());
        log.info("리뷰 수정 완료: reviewId={}, userId={}", reviewId, userId);
        return toResponseDto(savedReview, false, userId);
    }

    /** 리뷰 삭제 */
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("리뷰 삭제 권한이 없습니다.");
        }

        reviewKeywordRepository.deleteByReview(review);
        reviewLikeRepository.deleteByReview(review);
        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료: reviewId={}, userId={}", reviewId, userId);
    }

    /** 리뷰 조회 */
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    /** 리뷰 목록 조회 (페이지네이션) */
    @Transactional(readOnly = true)
    public Slice<ReviewResponseDto> findAll(Pageable pageable, String sort, String keyword, Long tmdbId) {
        List<Review> reviews;
        boolean hasNext;

        if (tmdbId != null) {
            // 영화별 리뷰 조회 (tmdbId 사용)
            List<Review> fetched = reviewRepository.findByMovieTmdbId(tmdbId, 
                    org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize() + 1));
            hasNext = fetched.size() > pageable.getPageSize();
            reviews = hasNext ? fetched.subList(0, pageable.getPageSize()) : fetched;
        } else if (keyword != null && !keyword.isBlank()) {
            // 키워드 필터링 (JOIN 쿼리 사용으로 성능 개선)
            List<Review> fetched = reviewRepository.findByKeywordName(keyword, 
                    org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize() + 1));
            hasNext = fetched.size() > pageable.getPageSize();
            reviews = hasNext ? fetched.subList(0, pageable.getPageSize()) : fetched;
        } else {
            // 정렬 옵션에 따른 조회
            Page<Review> page;
            if ("popular".equalsIgnoreCase(sort) || "views".equalsIgnoreCase(sort)) {
                // 인기순: 조회수 기준
                page = reviewRepository.findAllByOrderByViewCountDesc(pageable);
            } else if ("likes".equalsIgnoreCase(sort)) {
                // 좋아요순
                page = reviewRepository.findAllByOrderByLikeCountDesc(pageable);
            } else {
                // 최신순 (기본값)
                page = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
            reviews = page.getContent();
            hasNext = page.hasNext();
        }

        List<ReviewResponseDto> content = reviews.stream()
                .map(r -> toResponseDto(r, false, null))
                .collect(Collectors.toList());

        return new SliceImpl<>(content, pageable, hasNext);
    }

    /** 영화별 리뷰 목록 조회 */
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> findByMovieId(Long movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("영화를 찾을 수 없습니다: " + movieId));
        return reviewRepository.findByMovie(movie, pageable)
                .map(r -> toResponseDto(r, false, null));
    }

    /** 사용자별 리뷰 목록 조회 */
    @Transactional(readOnly = true)
    public Slice<ReviewResponseDto> findByUserId(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        Page<Review> page = reviewRepository.findByUser(user, pageable);
        List<ReviewResponseDto> content = page.getContent().stream()
                .map(r -> toResponseDto(r, false, userId))
                .collect(Collectors.toList());
        return new SliceImpl<>(content, pageable, page.hasNext());
    }

    /** 리뷰 조회수 증가 */
    public void incrementViewCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));
        Review updatedReview = review.toBuilder()
                .viewCount(review.getViewCount() + 1)
                .build();
        reviewRepository.save(updatedReview);
    }

    /** 리뷰 상세 조회 (조회수 증가 + 좋아요 여부 포함) */
    @Transactional
    public ReviewResponseDto getReviewDetail(Long reviewId, Long viewerUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));

        Review updatedReview = review.toBuilder()
                .viewCount(review.getViewCount() + 1)
                .build();
        Review saved = reviewRepository.save(updatedReview);

        boolean isLiked = false;
        if (viewerUserId != null) {
            User viewer = userRepository.findById(viewerUserId).orElse(null);
            if (viewer != null) {
                isLiked = reviewLikeRepository.findByReviewAndUser(saved, viewer).isPresent();
            }
        }

        return toResponseDto(saved, isLiked, viewerUserId);
    }

    /** 리뷰 좋아요 토글 */
    @Transactional
    public ReviewLikeResponseDto toggleLike(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        boolean liked = reviewLikeRepository.findByReviewAndUser(review, user).isPresent();
        if (liked) {
            reviewLikeRepository.deleteByReviewAndUser(review, user);
            Review updated = review.toBuilder()
                    .likeCount(Math.max(0, review.getLikeCount() - 1))
                    .build();
            Review saved = reviewRepository.save(updated);
            return new ReviewLikeResponseDto(false, saved.getLikeCount());
        }

        ReviewLike reviewLike = ReviewLike.builder()
                .review(review)
                .user(user)
                .build();
        reviewLikeRepository.save(reviewLike);

        Review updated = review.toBuilder()
                .likeCount(review.getLikeCount() + 1)
                .build();
        Review saved = reviewRepository.save(updated);
        return new ReviewLikeResponseDto(true, saved.getLikeCount());
    }

    private void updateReviewKeywords(Review review, List<String> keywords) {
        reviewKeywordRepository.deleteByReview(review);
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        for (String keywordName : keywords) {
            if (keywordName == null || keywordName.isBlank()) continue;
            Keyword keyword = keywordService.getOrCreateKeyword(keywordName);
            ReviewKeyword reviewKeyword = ReviewKeyword.builder()
                    .review(review)
                    .keyword(keyword)
                    .build();
            reviewKeywordRepository.save(reviewKeyword);
        }
    }

    private ReviewResponseDto toResponseDto(Review review, boolean isLiked, Long viewerUserId) {
        List<String> keywords = reviewKeywordRepository.findByReview(review).stream()
                .map(rk -> rk.getKeyword().getName())
                .collect(Collectors.toList());

        return new ReviewResponseDto(
                review.getReviewId(),
                review.getTitle(),
                review.getContent(),
                review.getScore(),
                review.getViewCount(),
                review.getLikeCount(),
                viewerUserId == null ? false : isLiked,
                new ReviewAuthorDto(review.getUser().getUserId(), review.getUser().getNickname()),
                new ReviewMovieDto(
                        review.getMovie().getTmdbId(),
                        review.getMovie().getTitle(),
                        review.getMovie().getPosterUrl()
                ),
                keywords,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
