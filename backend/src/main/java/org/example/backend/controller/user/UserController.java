package org.example.backend.controller.user;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.genre.GenreResponseDto;
import org.example.backend.dto.genre.UserGenreRequestDto;
import org.example.backend.dto.keyword.KeywordResponseDto;
import org.example.backend.dto.movie.WatchedMovieListItemResponseDto;
import org.example.backend.dto.review.ReviewResponseDto;
import org.example.backend.dto.user.UserResponseDto;
import org.example.backend.security.CustomPrincipal;
import org.example.backend.service.genre.GenreService;
import org.example.backend.service.keyword.KeywordService;
import org.example.backend.service.movie.WatchedMovieService;
import org.example.backend.service.review.ReviewService;
import org.example.backend.service.user.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GenreService genreService;
    private final KeywordService keywordService;
    private final ReviewService reviewService;
    private final WatchedMovieService watchedMovieService;

    /** 현재 로그인한 사용자 정보 조회 */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return ResponseEntity.ok(new UserResponseDto(
                customPrincipal.getUser().getUserId(),
                customPrincipal.getUser().getEmail(),
                customPrincipal.getUser().getNickname(),
                customPrincipal.getUser().getProvider()
        ));
    }

    /** 사용자 프로필 수정 */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMe(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody org.example.backend.dto.user.UserUpdateRequestDto requestDto
    ) {
        var updated = userService.updateNickname(customPrincipal.getUserId(), requestDto.nickname());
        return ResponseEntity.ok(new UserResponseDto(
                updated.getUserId(),
                updated.getEmail(),
                updated.getNickname(),
                updated.getProvider()
        ));
    }

    /** 관심 장르 목록 조회 */
    @GetMapping("/me/genres")
    public ResponseEntity<List<GenreResponseDto>> getMyGenres(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return ResponseEntity.ok(genreService.getMyGenres(customPrincipal.getUserId()));
    }

    /** 관심 장르 등록 */
    @PostMapping("/me/genres")
    public ResponseEntity<Void> addMyGenre(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody UserGenreRequestDto requestDto
    ) {
        genreService.createMyGenre(customPrincipal.getUserId(), requestDto.genreId());
        return ResponseEntity.ok().build();
    }

    /** 관심 장르 삭제 */
    @DeleteMapping("/me/genres/{genreId}")
    public ResponseEntity<Void> deleteMyGenre(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @PathVariable Long genreId
    ) {
        genreService.deleteMyGenre(customPrincipal.getUserId(), genreId);
        return ResponseEntity.ok().build();
    }

    /** 관심 키워드 목록 조회 */
    @GetMapping("/me/keywords")
    public ResponseEntity<List<KeywordResponseDto>> getMyKeywords(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return ResponseEntity.ok(keywordService.getMyKeywords(customPrincipal.getUserId()));
    }

    /** 관심 키워드 등록 */
    @PostMapping("/me/keywords/{keywordName}")
    public ResponseEntity<Void> addMyKeyword(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @PathVariable String keywordName
    ) {
        keywordService.createMyKeyword(customPrincipal.getUserId(), keywordName);
        return ResponseEntity.ok().build();
    }

    /** 관심 키워드 삭제 */
    @DeleteMapping("/me/keywords/{keywordName}")
    public ResponseEntity<Void> deleteMyKeyword(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @PathVariable String keywordName
    ) {
        keywordService.deleteMyKeyword(customPrincipal.getUserId(), keywordName);
        return ResponseEntity.ok().build();
    }

    /** 내가 작성한 리뷰 목록 조회 */
    @GetMapping("/me/reviews")
    public ResponseEntity<Slice<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.findByUserId(customPrincipal.getUserId(), pageable));
    }

    /** 내가 본 영화 목록 조회 */
    @GetMapping("/me/watched-movies")
    public ResponseEntity<Slice<WatchedMovieListItemResponseDto>> getMyWatchedMovies(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            Pageable pageable
    ) {
        return ResponseEntity.ok(watchedMovieService.getWatchedMovies(customPrincipal.getUserId(), pageable));
    }
}

