package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.tmdb.TmdbGenreResponseDto;
import org.example.backend.dto.tmdb.TmdbMovieResponseDto;
import org.example.backend.service.tmdb.TmdbApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbApiService tmdbApiService;

    @GetMapping("/movies/popular")
    public ResponseEntity<List<TmdbMovieResponseDto>> getPopularMovies(
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        List<TmdbMovieResponseDto> movies = tmdbApiService.getPopularMovies(page);
        // 테스트 확인 20개 받아옴.. 우선 10개 이하로
        List<TmdbMovieResponseDto> limitedMovies = movies.size() > 10 
                ? movies.subList(0, 10) 
                : movies;
        return ResponseEntity.ok(limitedMovies); // 200
    }

    @GetMapping("/movies/now-playing")
    public ResponseEntity<List<TmdbMovieResponseDto>> getNowPlayingMovies(
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        List<TmdbMovieResponseDto> movies = tmdbApiService.getNowPlayingMovies(page);
        // 테스트 확인 20개 받아옴.. 우선 10개 이하로
        List<TmdbMovieResponseDto> limitedMovies = movies.size() > 10 
                ? movies.subList(0, 10) 
                : movies;
        return ResponseEntity.ok(limitedMovies); // 200
    }

    @GetMapping("/movies/{tmdbId}")
    public ResponseEntity<TmdbMovieResponseDto> getMovieDetail(
            @PathVariable Long tmdbId
    ) {
        TmdbMovieResponseDto movie = tmdbApiService.getMovieDetail(tmdbId);
        return ResponseEntity.ok(movie); // 200
    }

    @GetMapping("/genres")
    public ResponseEntity<List<TmdbGenreResponseDto.Genre>> getGenres() {
        List<TmdbGenreResponseDto.Genre> genres = tmdbApiService.getGenres();
        return ResponseEntity.ok(genres); // 200
    }
}

