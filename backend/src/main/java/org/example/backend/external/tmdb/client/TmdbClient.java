package org.example.backend.external.tmdb.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.external.tmdb.dto.TmdbGenreDto;
import org.example.backend.external.tmdb.dto.TmdbGenreResponseDto;
import org.example.backend.external.tmdb.dto.TmdbMovieListResponseDto;
import org.example.backend.external.tmdb.dto.TmdbMovieResponseDto;
import org.example.backend.external.tmdb.dto.TmdbVideoDto;
import org.example.backend.external.tmdb.dto.TmdbVideoListResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    /** 개봉 예정 영화 목록 조회 */
    public Slice<TmdbMovieResponseDto> fetchUpcomingMovies(Pageable pageable) {
        return fetchMovies("/movie/upcoming", pageable, "ko-KR", "KR");
    }

    /** 영화 상세 정보 조회 (한국어 우선, 없으면 영어) */
    public TmdbMovieResponseDto fetchMovieDetail(Long tmdbId) {
        try {
            // 먼저 한국어로 요청
            TmdbMovieResponseDto koreanMovie = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("language", "ko-KR")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(TmdbMovieResponseDto.class)
                    .block();
            
            // 한국어 overview가 비어있거나 null이면 영어 버전으로 재요청
            if (koreanMovie != null && (koreanMovie.overview() == null || koreanMovie.overview().isBlank())) {
                TmdbMovieResponseDto englishMovie = tmdbWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/{id}")
                                .queryParam("api_key", tmdbApiKey)
                                .queryParam("language", "en-US")
                                .build(tmdbId))
                        .retrieve()
                        .bodyToMono(TmdbMovieResponseDto.class)
                        .block();
                
                // 영어 버전의 overview와 title을 사용 (한국어 제목이 비어있을 경우)
                if (englishMovie != null) {
                    String title = (koreanMovie.title() == null || koreanMovie.title().isBlank()) 
                            ? englishMovie.title() 
                            : koreanMovie.title();
                    String overview = englishMovie.overview();
                    
                    return new TmdbMovieResponseDto(
                            koreanMovie.id(),
                            title,
                            koreanMovie.releaseDate(),
                            koreanMovie.posterPath(),
                            overview,
                            koreanMovie.voteAverage(),
                            koreanMovie.voteCount(),
                            koreanMovie.runtime(),
                            koreanMovie.genreIds()
                    );
                }
            }
            
            return koreanMovie;
        } catch (WebClientResponseException e) {
            log.error("tmdb api 호출 실패: 영화 상세 조회 - tmdbId: {}", tmdbId, e);
            throw new RuntimeException("tmdb api 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 영화 영상(트레일러/티저 등) 목록 조회 (한국어 우선, 없으면 영어) */
    public List<TmdbVideoDto> fetchMovieVideos(Long tmdbId) {
        try {
            // 먼저 한국어로 요청
            TmdbVideoListResponseDto koreanResponse = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}/videos")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("language", "ko-KR")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(TmdbVideoListResponseDto.class)
                    .block();

            List<TmdbVideoDto> koreanVideos = koreanResponse != null && koreanResponse.results() != null 
                    ? koreanResponse.results() 
                    : List.of();
            
            // 한국어 영상이 없거나 비어있으면 영어 버전으로 재요청
            if (koreanVideos.isEmpty()) {
                TmdbVideoListResponseDto englishResponse = tmdbWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/{id}/videos")
                                .queryParam("api_key", tmdbApiKey)
                                .queryParam("language", "en-US")
                                .build(tmdbId))
                        .retrieve()
                        .bodyToMono(TmdbVideoListResponseDto.class)
                        .block();
                
                return englishResponse != null && englishResponse.results() != null 
                        ? englishResponse.results() 
                        : List.of();
            }
            
            return koreanVideos;
        } catch (WebClientResponseException e) {
            log.error("tmdb api 호출 실패: 영화 영상 조회 - tmdbId: {}", tmdbId, e);
            throw new RuntimeException("tmdb api 호출 실패: " + e.getMessage(), e);
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
            log.error("tmdb api 호출 실패: 장르 목록 조회", e);
            throw new RuntimeException("tmdb api 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 영화 검색 (제목 검색) */
    public Slice<TmdbMovieResponseDto> searchMovies(String query, Pageable pageable) {
        try {
            int page = pageable.getPageNumber() + 1;
            int size = pageable.getPageSize();

            TmdbMovieListResponseDto response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/movie")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("query", query)
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build())
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
            log.error("tmdb api 호출 실패: 영화 검색 - query: {}", query, e);
            throw new RuntimeException("tmdb api 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 영화 찾기 (장르, 연도 범위, 정렬 필터링) */
    public Slice<TmdbMovieResponseDto> discoverMovies(
            Pageable pageable,
            Long genreId,
            Integer releaseYearFrom,
            Integer releaseYearTo,
            String sortBy
    ) {
        try {
            int page = pageable.getPageNumber() + 1;
            int size = pageable.getPageSize();
            
            // 오늘 날짜
            LocalDate todayDate = LocalDate.now();
            String todayStr = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            var requestSpec = tmdbWebClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/discover/movie")
                                .queryParam("api_key", tmdbApiKey)
                                .queryParam("page", page)
                                .queryParam("language", "ko-KR")
                                .queryParam("include_adult", false)
                                .queryParam("sort_by", convertSortBy(sortBy));

                        // 장르 필터
                        if (genreId != null) {
                            builder.queryParam("with_genres", genreId);
                        }
                        
                        // 연도 범위 필터
                        if (releaseYearFrom != null) {
                            builder.queryParam("primary_release_date.gte", releaseYearFrom + "-01-01");
                        }
                        
                        if (releaseYearTo != null) {
                            String endDate = releaseYearTo + "-12-31";
                            // 미래 날짜는 오늘까지만
                            if (endDate.compareTo(todayStr) > 0) {
                                builder.queryParam("primary_release_date.lte", todayStr);
                            } else {
                                builder.queryParam("primary_release_date.lte", endDate);
                            }
                        } else {
                            // releaseYearTo가 없으면 오늘까지만 (미래 영화 제외)
                            builder.queryParam("primary_release_date.lte", todayStr);
                        }
                        
                        // 평점순일 때 최소 투표 수 필터
                        if ("rating".equalsIgnoreCase(sortBy)) {
                            builder.queryParam("vote_count.gte", 200);
                        }

                        return builder.build();
                    });

            TmdbMovieListResponseDto response = requestSpec
                    .retrieve()
                    .bodyToMono(TmdbMovieListResponseDto.class)
                    .block();

            if (response == null || response.results() == null) {
                log.warn("tmdb api 응답이 null이거나 results가 null입니다. genreId: {}, sortBy: {}", genreId, sortBy);
                return new SliceImpl<>(List.of(), pageable, false);
            }

            List<TmdbMovieResponseDto> results = response.results();
            log.debug("tmdb api 응답: {}개 영화 조회됨 (genreId: {}, sortBy: {})", results.size(), genreId, sortBy);
            
            List<TmdbMovieResponseDto> content = results.size() > size
                    ? results.subList(0, size)
                    : results;

            boolean hasNext = response.page() < response.totalPages() && results.size() > size;
            return new SliceImpl<>(content, pageable, hasNext);
        } catch (WebClientResponseException e) {
            log.error("tmdb api 호출 실패: 영화 찾기 - genreId: {}, releaseYearFrom: {}, releaseYearTo: {}, sortBy: {}", 
                    genreId, releaseYearFrom, releaseYearTo, sortBy, e);
            throw new RuntimeException("tmdb api 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 정렬 옵션을 tmdb api 형식으로 변환 */
    private String convertSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "release_date.desc"; // 최신순
        }
        return switch (sortBy.toLowerCase()) {
            case "rating" -> "vote_average.desc"; // 평점순
            case "latest" -> "release_date.desc"; // 최신순
            default -> "release_date.desc";
        };
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
            log.error("tmdb api 호출 실패: 영화 목록 조회 - path: {}", path, e);
            throw new RuntimeException("tmdb api 호출 실패: " + e.getMessage(), e);
        }
    }
}

