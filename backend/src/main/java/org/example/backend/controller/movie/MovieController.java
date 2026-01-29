package org.example.backend.controller.movie;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.movie.MovieDetailResponseDto;
import org.example.backend.dto.movie.MovieResponseDto;
import org.example.backend.security.CustomPrincipal;
import org.example.backend.service.movie.MovieService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /** 영화 찾기 (제목, 장르, 연도, 정렬 필터링) */
    @GetMapping
    public ResponseEntity<Slice<MovieResponseDto>> getMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer releaseYearFrom,
            @RequestParam(required = false) Integer releaseYearTo,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            Pageable pageable
    ) {
        return ResponseEntity.ok(movieService.getMovies(keyword, genreId, releaseYearFrom, releaseYearTo, sortBy, pageable));
    }

    /** 전체 인기작 영화 목록 조회 */
    @GetMapping("/top-rated")
    public ResponseEntity<Slice<MovieResponseDto>> getTopRatedMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getTopRatedMovies(pageable));
    }

    /** 인기 상영작 목록 조회 */
    @GetMapping("/now-playing")
    public ResponseEntity<Slice<MovieResponseDto>> getNowPlayingMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getNowPlayingMovies(pageable));
    }

    /** 인기 상영작 목록 조회 (영화 목록 페이지용) */
    @GetMapping("/now-playing/list")
    public ResponseEntity<Slice<MovieResponseDto>> getNowPlayingMoviesList(Pageable pageable) {
        return ResponseEntity.ok(movieService.getNowPlayingMoviesList(pageable));
    }

    /** 개봉 예정 영화 목록 조회 (영화 목록 페이지용) */
    @GetMapping("/upcoming/list")
    public ResponseEntity<Slice<MovieResponseDto>> getUpcomingMoviesList(Pageable pageable) {
        return ResponseEntity.ok(movieService.getUpcomingMoviesList(pageable));
    }

    /** 전체 인기작 목록 조회 (영화 목록 페이지용) */
    @GetMapping("/top-rated/list")
    public ResponseEntity<Slice<MovieResponseDto>> getTopRatedMoviesList(Pageable pageable) {
        return ResponseEntity.ok(movieService.getTopRatedMoviesList(pageable));
    }

    /** 영화 상세 조회 */
    @GetMapping("/{tmdbId}")
    public ResponseEntity<MovieDetailResponseDto> getMovieDetail(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestParam(defaultValue = "0") int commentPage,
            @RequestParam(defaultValue = "10") int commentSize
    ) {
        Long userId = customPrincipal != null ? customPrincipal.getUserId() : null;
        return ResponseEntity.ok(movieService.getMovieDetail(tmdbId, userId, commentPage, commentSize));
    }
}


