package org.example.backend.controller.movie;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.movie.MovieUserRatingUpsertRequestDto;
import org.example.backend.security.CustomPrincipal;
import org.example.backend.service.movie.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final MovieService movieService;

    /** 평점/코멘트 등록/수정 */
    @PostMapping
    public ResponseEntity<Void> upsertRating(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestBody MovieUserRatingUpsertRequestDto requestDto
    ) {
        movieService.upsertUserRating(customPrincipal.getUserId(), requestDto);
        return ResponseEntity.ok().build();
    }

    /** 평점/코멘트 삭제 */
    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<Void> deleteRating(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @PathVariable Long tmdbId
    ) {
        movieService.deleteUserRating(customPrincipal.getUserId(), tmdbId);
        return ResponseEntity.ok().build();
    }
}


