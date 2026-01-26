package org.example.backend.external.tmdb.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.external.tmdb.dto.TmdbGenreDto;
import org.example.backend.external.tmdb.dto.TmdbGenreResponseDto;
import org.example.backend.external.tmdb.dto.TmdbMovieListResponseDto;
import org.example.backend.external.tmdb.dto.TmdbMovieResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TmdbClient {

    private final WebClient tmdbWebClient;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    /** 인기 영화 목록 조회 */
    public Slice<TmdbMovieResponseDto> fetchPopularMovies(Pageable pageable) {
        return fetchMovies("/movie/popular", pageable, "ko-KR", null);
    }

    /** 개봉 중인 영화 목록 조회 */
    public Slice<TmdbMovieResponseDto> fetchNowPlayingMovies(Pageable pageable) {
        return fetchMovies("/movie/now_playing", pageable, "ko-KR", "KR");
    }

    /** 영화 상세 정보 조회 */
    public TmdbMovieResponseDto fetchMovieDetail(Long tmdbId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("language", "ko-KR")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(TmdbMovieResponseDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 영화 상세 조회 - tmdbId: {}", tmdbId, e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 장르 목록 조회 */
    public List<TmdbGenreDto> fetchGenres() {
        try {
            TmdbGenreResponseDto response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/genre/movie/list")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbGenreResponseDto.class)
                    .block();

            return response != null ? response.genres() : List.of();
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 장르 목록 조회", e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 공통 영화 목록 반환 처리 */
    private Slice<TmdbMovieResponseDto> fetchMovies(
            String path, Pageable pageable, String language, String region) {
        try {
            int page = pageable.getPageNumber() + 1;
            int size = pageable.getPageSize();

            var requestSpec = tmdbWebClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path(path)
                                .queryParam("api_key", tmdbApiKey)
                                .queryParam("page", page)
                                .queryParam("language", language);
                        if (region != null) {
                            builder.queryParam("region", region);
                        }
                        return builder.build();
                    });

            TmdbMovieListResponseDto response = requestSpec
                    .retrieve()
                    .bodyToMono(TmdbMovieListResponseDto.class)
                    .block();

            if (response == null || response.results() == null) {
                return new SliceImpl<>(List.of(), pageable, false);
            }

            List<TmdbMovieResponseDto> results = response.results();
            List<TmdbMovieResponseDto> content = results.size() > size
                    ? results.subList(0, size)
                    : results;

            boolean hasNext = response.page() < response.totalPages() && results.size() > size;
            return new SliceImpl<>(content, pageable, hasNext);
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 영화 목록 조회 - path: {}", path, e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }
}

