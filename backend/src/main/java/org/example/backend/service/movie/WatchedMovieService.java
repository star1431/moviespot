package org.example.backend.service.movie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.movie.WatchedMovie;
import org.example.backend.domain.user.User;
import org.example.backend.dto.movie.WatchedMovieListItemResponseDto;
import org.example.backend.dto.movie.WatchedMovieUpsertRequestDto;
import org.example.backend.repository.movie.WatchedMovieRepository;
import org.example.backend.repository.user.UserRepository;
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
public class WatchedMovieService {

    private final MovieService movieService;
    private final UserRepository userRepository;
    private final WatchedMovieRepository watchedMovieRepository;

    /** 본 영화 등록/수정 */
    public void upsertWatchedMovie(Long userId, WatchedMovieUpsertRequestDto requestDto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        // 영화 조회 또는 생성
        Movie movie = movieService.getOrCreateMovieByTmdbId(requestDto.tmdbId());
        
        // 기존 본 영화 조회
        Optional<WatchedMovie> existingWatchedOpt = watchedMovieRepository
                .findByUserUserIdAndMovieTmdbId(userId, requestDto.tmdbId());
        
        if (existingWatchedOpt.isPresent()) {
            // 업데이트
            WatchedMovie existingWatched = existingWatchedOpt.get();
            WatchedMovie updatedWatched = WatchedMovie.builder()
                    .watchedMovieId(existingWatched.getWatchedMovieId())
                    .user(existingWatched.getUser())
                    .movie(existingWatched.getMovie())
                    .createdAt(existingWatched.getCreatedAt())
                    .build();
            watchedMovieRepository.save(updatedWatched);
            log.info("본 영화 수정 완료: userId={}, tmdbId={}", userId, requestDto.tmdbId());
        } else {
            // 신규 저장
            WatchedMovie newWatched = WatchedMovie.builder()
                    .user(user)
                    .movie(movie)
                    .build();
            watchedMovieRepository.save(newWatched);
            log.info("본 영화 등록 완료: userId={}, tmdbId={}", userId, requestDto.tmdbId());
        }
    }

    /** 본 영화 삭제 */
    public void deleteWatchedMovie(Long userId, Long tmdbId) {
        watchedMovieRepository.deleteByUserUserIdAndMovieTmdbId(userId, tmdbId);
        log.info("본 영화 삭제 완료: userId={}, tmdbId={}", userId, tmdbId);
    }

    /** 본 영화 목록 조회 */
    @Transactional(readOnly = true)
    public Slice<WatchedMovieListItemResponseDto> getWatchedMovies(Long userId, Pageable pageable) {
        List<WatchedMovie> watchedMovies = watchedMovieRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        List<WatchedMovieListItemResponseDto> content = watchedMovies.stream()
                .map(w -> new WatchedMovieListItemResponseDto(
                        w.getMovie().getTmdbId(),
                        w.getMovie().getTitle(),
                        w.getMovie().getPosterUrl(),
                        w.getMovie().getTmdbRate(),
                        w.getCreatedAt()  // 본 영화 등록일시
                ))
                .collect(Collectors.toList());

        boolean hasNext = content.size() == pageable.getPageSize();
        return new SliceImpl<>(content, pageable, hasNext);
    }
}