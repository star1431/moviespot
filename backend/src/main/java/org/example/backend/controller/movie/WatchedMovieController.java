package org.example.backend.controller.movie;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.movie.WatchedMovieListItemResponseDto;
import org.example.backend.dto.movie.WatchedMovieUpsertRequestDto;
import org.example.backend.security.CustomPrincipal;
import org.example.backend.service.movie.WatchedMovieService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watched-movies")
@RequiredArgsConstructor
public class WatchedMovieController {

    private final WatchedMovieService watchedMovieService;

    /** 본 영화 등록/수정 */
    @PostMapping
    public ResponseEntity<Void> upsertWatchedMovie(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody WatchedMovieUpsertRequestDto requestDto
    ) {
        watchedMovieService.upsertWatchedMovie(customPrincipal.getUserId(), requestDto);
        return ResponseEntity.ok().build();
    }

    /** 본 영화 삭제 */
    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<Void> deleteWatchedMovie(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @PathVariable Long tmdbId
    ) {
        watchedMovieService.deleteWatchedMovie(customPrincipal.getUserId(), tmdbId);
        return ResponseEntity.ok().build();
    }

    /** 본 영화 목록 조회 */
    @GetMapping
    public ResponseEntity<Slice<WatchedMovieListItemResponseDto>> getWatchedMovies(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            Pageable pageable
    ) {
        return ResponseEntity.ok(watchedMovieService.getWatchedMovies(customPrincipal.getUserId(), pageable));
    }
}


