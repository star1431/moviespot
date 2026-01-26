package org.example.backend.controller.tmdb;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.tmdb.TmdbGenreDto;
import org.example.backend.dto.tmdb.TmdbMovieResponseDto;
import org.example.backend.service.tmdb.TmdbApiService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbApiService tmdbApiService;

    @GetMapping("/movies/popular")
    public ResponseEntity<Slice<TmdbMovieResponseDto>> getPopularMovies(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<TmdbMovieResponseDto> movies = tmdbApiService.getPopularMovies(pageable);
        return ResponseEntity.ok(movies); // 200
    }

    @GetMapping("/movies/now-playing")
    public ResponseEntity<Slice<TmdbMovieResponseDto>> getNowPlayingMovies(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<TmdbMovieResponseDto> movies = tmdbApiService.getNowPlayingMovies(pageable);
        return ResponseEntity.ok(movies); // 200
    }

    @GetMapping("/movies/{tmdbId}")
    public ResponseEntity<TmdbMovieResponseDto> getMovieDetail(
            @PathVariable Long tmdbId
    ) {
        TmdbMovieResponseDto movie = tmdbApiService.getMovieDetail(tmdbId);
        return ResponseEntity.ok(movie); // 200
    }

    @GetMapping("/genres")
    public ResponseEntity<List<TmdbGenreDto>> getGenres() {
        List<TmdbGenreDto> genres = tmdbApiService.getGenres();
        return ResponseEntity.ok(genres); // 200
    }
}

