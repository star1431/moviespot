package org.example.backend.service.tmdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.genre.Genre;
import org.example.backend.domain.movie.Movie;
import org.example.backend.dto.tmdb.TmdbGenreDto;
import org.example.backend.dto.tmdb.TmdbGenreResponseDto;
import org.example.backend.dto.tmdb.TmdbMovieListResponseDto;
import org.example.backend.dto.tmdb.TmdbMovieResponseDto;
import org.example.backend.repository.genre.GenreRepository;
import org.example.backend.repository.movie.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TmdbApiService {

    private final WebClient tmdbWebClient;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Value("${tmdb.api.image-base-url}")
    private String tmdbImageBaseUrl;

    /** 인기 영화 - db 우선 조회 후 없으면 api 호출 */
    @Transactional(readOnly = true)
    public Slice<TmdbMovieResponseDto> getPopularMovies(Pageable pageable) {
        // db에 1일 이내 업데이트된거 있으면 유지 아니면 조회
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long recentMovieCount = movieRepository.countByUpdatedAtAfter(oneDayAgo);
        
        if (recentMovieCount >= 20) {
            List<Movie> movies = movieRepository.findAllByOrderByCreatedAtDesc(pageable);
            List<TmdbMovieResponseDto> content = movies.stream()
                    .map(movie -> TmdbMovieResponseDto.from(movie, tmdbImageBaseUrl))
                    .collect(Collectors.toList());
            
            boolean hasNext = content.size() == pageable.getPageSize();
            return new SliceImpl<>(content, pageable, hasNext);
        }
        
        // DB에 데이터가 없거나 오래된 경우 API 호출
        log.info("TMDB API 호출: 인기 영화 조회 (DB 데이터 부족)");
        return fetchPopularMoviesFromApi(pageable);
    }
    
    /** tmdb api - 인기 영화 목록 조회 */
    private Slice<TmdbMovieResponseDto> fetchPopularMoviesFromApi(Pageable pageable) {
        try {
            int page = pageable.getPageNumber() + 1; // tmdb는 1부터..
            int size = pageable.getPageSize();
            
            TmdbMovieListResponseDto response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/popular")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()  // 요청 받
                    .bodyToMono(TmdbMovieListResponseDto.class)  // 응답본문 dto로
                    .block(); // 블로킹 방식으로 데이터 받음

            if (response == null || response.results() == null) {
                return new SliceImpl<>(List.of(), pageable, false);
            }

            List<TmdbMovieResponseDto> results = response.results();
            List<TmdbMovieResponseDto> content = results.size() > size 
                    ? results.subList(0, size) 
                    : results;
            
            // 목록 조회는 DB 저장하지 않음 (필터링/정렬 시 API만 호출)
            
            // 다음 페이지가 있는지 확인
            boolean hasNext = response.page() < response.totalPages() && results.size() > size;
            
            return new SliceImpl<>(content, pageable, hasNext);
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 인기 영화 조회", e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 최신 개봉일 영화 -  db 우선 조회 후 없으면 api 호출  */
    @Transactional(readOnly = true)
    public Slice<TmdbMovieResponseDto> getNowPlayingMovies(Pageable pageable) {
        // db에 1일 이내 업데이트된거 있으면 유지 아니면 조회
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long recentMovieCount = movieRepository.countByUpdatedAtAfter(oneDayAgo);
        
        if (recentMovieCount >= 20) {
            log.info("DB에서 개봉 중인 영화 조회 (최근 업데이트된 영화: {}개)", recentMovieCount);
            List<Movie> movies = movieRepository.findAllByOrderByCreatedAtDesc(pageable);
            List<TmdbMovieResponseDto> content = movies.stream()
                    .map(movie -> TmdbMovieResponseDto.from(movie, tmdbImageBaseUrl))
                    .collect(Collectors.toList());
            
            boolean hasNext = content.size() == pageable.getPageSize();
            return new SliceImpl<>(content, pageable, hasNext);
        }
        
        // DB에 데이터가 없거나 오래된 경우 API 호출
        log.info("TMDB API 호출: 개봉 중인 영화 조회 (DB 데이터 부족)");
        return fetchNowPlayingMoviesFromApi(pageable);
    }
    
    /** tmdb api - 최신 개봉일 영화 목록 조회 */
    private Slice<TmdbMovieResponseDto> fetchNowPlayingMoviesFromApi(Pageable pageable) {
        try {
            int page = pageable.getPageNumber() + 1;
            int size = pageable.getPageSize();
            
            TmdbMovieListResponseDto response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/now_playing")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("page", page)
                            .queryParam("region", "KR")
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
            
            // 목록 조회는 DB 저장하지 않음 (필터링/정렬 시 API만 호출)
            
            // 다음 페이지가 있는지 확인
            boolean hasNext = response.page() < response.totalPages() && results.size() > size;
            
            return new SliceImpl<>(content, pageable, hasNext);
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 개봉 중인 영화 조회", e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** tmdb api - 영화 상세 */
    @Transactional(readOnly = true)
    public TmdbMovieResponseDto getMovieDetail(Long tmdbId) {
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

    /** 장르 목록 - db 우선 조회 후 없으면 api 호출 */
    @Transactional(readOnly = true)
    public List<TmdbGenreDto> getGenres() {
        List<Genre> dbGenres = genreRepository.findAll();
        if (!dbGenres.isEmpty()) {
            return dbGenres.stream()
                    .map(TmdbGenreDto::from)
                    .collect(Collectors.toList());
        }
        
        // db에 없으면...
        return fetchGenresFromApi();
    }
    
    /** tmdb api - 장르 목록 조회 */
    private List<TmdbGenreDto> fetchGenresFromApi() {
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

            List<TmdbGenreDto> genres = response != null ? response.genres() : List.of();

            if (!genres.isEmpty()) {
                genres.forEach(this::saveOrUpdateGenre);
            }
            
            return genres;
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 장르 목록 조회", e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }


    /** 영화 엔티티 저장 or 업데이트 */
    public Movie saveOrUpdateMovie(TmdbMovieResponseDto tmdbMovie) {
        return movieRepository.findByTmdbId(tmdbMovie.id())
                .map(existingMovie -> {
                    Movie updatedMovie = existingMovie.toBuilder()
                            .title(tmdbMovie.title())
                            .releaseDate(tmdbMovie.releaseDate())
                            .posterUrl(tmdbMovie.posterPath() != null 
                                    ? tmdbImageBaseUrl + tmdbMovie.posterPath() 
                                    : null)
                            .overview(tmdbMovie.overview())
                            .tmdbRate(tmdbMovie.voteAverage())
                            .build();
                    return movieRepository.save(updatedMovie);
                })
                .orElseGet(() -> {
                    Movie newMovie = tmdbMovie.toMovie(tmdbImageBaseUrl);
                    return movieRepository.save(newMovie);
                });
    }

    /** 장르 엔티티 저장 or 업데이트 */
    public Genre saveOrUpdateGenre(TmdbGenreDto tmdbGenre) {
        return genreRepository.findByTmdbGenreId(tmdbGenre.id())
                .orElseGet(() -> {
                    Genre newGenre = tmdbGenre.toGenre();
                    return genreRepository.save(newGenre);
                });
    }
}

