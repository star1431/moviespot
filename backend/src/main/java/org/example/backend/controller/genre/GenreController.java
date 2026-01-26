package org.example.backend.controller.genre;

import lombok.RequiredArgsConstructor;
import org.example.backend.external.tmdb.dto.TmdbGenreDto;
import org.example.backend.service.genre.GenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    /** 장르 목록 조회 */
    @GetMapping
    public ResponseEntity<List<TmdbGenreDto>> getGenres() {
        return ResponseEntity.ok(genreService.getGenres());
    }
}


