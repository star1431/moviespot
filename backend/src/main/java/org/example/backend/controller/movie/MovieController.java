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

    /** 전체 인기작 목록 조회 */
    @GetMapping("/popular")
    public ResponseEntity<Slice<MovieResponseDto>> getPopularMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getPopularMovies(pageable));
    }

    /** 인기 상영작 목록 조회 */
    @GetMapping("/now-playing")
    public ResponseEntity<Slice<MovieResponseDto>> getNowPlayingMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getNowPlayingMovies(pageable));
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


