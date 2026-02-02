package org.example.backend.controller.review;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.review.ReviewCreateRequestDto;
import org.example.backend.dto.review.ReviewLikeResponseDto;
import org.example.backend.dto.review.ReviewResponseDto;
import org.example.backend.dto.review.ReviewUpdateRequestDto;
import org.example.backend.security.CustomPrincipal;
import org.example.backend.service.review.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 리뷰 작성 */
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody ReviewCreateRequestDto requestDto
    ) {
        return ResponseEntity.status(201).body(reviewService.createReview(customPrincipal.getUserId(), requestDto));
    }

    /** 리뷰 수정 */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody ReviewUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, customPrincipal.getUserId(), requestDto));
    }

    /** 리뷰 삭제 */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        reviewService.deleteReview(reviewId, customPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    /** 리뷰 목록 조회 */
    @GetMapping
    public ResponseEntity<Slice<ReviewResponseDto>> getReviews(
            Pageable pageable,
            // NOTE: sort는 Spring Pageable 예약 파라미터라서 사용하면
            // sort=latest 같은 값이 엔티티 필드 정렬로 해석되어 500이 발생할 수 있음
            @RequestParam(required = false, name = "sortBy") String sortBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tmdbId,
            @RequestParam(required = false) String movieTitle
    ) {
        return ResponseEntity.ok(reviewService.findAll(pageable, sortBy, keyword, tmdbId, movieTitle));
    }

    /** 리뷰 상세 조회  */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewDetail(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        Long userId = customPrincipal != null ? customPrincipal.getUserId() : null;
        return ResponseEntity.ok(reviewService.getReviewDetail(reviewId, userId));
    }

    /** 리뷰 좋아요/취소 (토글) */
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeResponseDto> toggleLike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return ResponseEntity.ok(reviewService.toggleLike(reviewId, customPrincipal.getUserId()));
    }
}


