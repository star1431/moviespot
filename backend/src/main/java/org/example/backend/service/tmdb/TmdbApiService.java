package org.example.backend.service.tmdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.movie.Movie;
import org.example.backend.dto.tmdb.TmdbGenreResponseDto;
import org.example.backend.dto.tmdb.TmdbMovieListResponseDto;
import org.example.backend.dto.tmdb.TmdbMovieResponseDto;
import org.example.backend.repository.movie.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbApiService {

    private final WebClient tmdbWebClient;
    private final MovieRepository movieRepository;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Value("${tmdb.api.image-base-url}")
    private String tmdbImageBaseUrl;

    /** tmdb api - 인기 영화 목록 */
    public List<TmdbMovieResponseDto> getPopularMovies(Integer page) {
        try {
            TmdbMovieListResponseDto response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/popular")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("page", page != null ? page : 1)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbMovieListResponseDto.class)
                    .block();

            return response != null ? response.results() : List.of();
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 인기 영화 조회", e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** tmdb api - 개봉 중인 영화 목록 */
    public List<TmdbMovieResponseDto> getNowPlayingMovies(Integer page) {
        try {
            TmdbMovieListResponseDto response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/now_playing")
                            .queryParam("api_key", tmdbApiKey)
                            .queryParam("page", page != null ? page : 1)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbMovieListResponseDto.class)
                    .block();

            return response != null ? response.results() : List.of();
        } catch (WebClientResponseException e) {
            log.error("TMDB API 호출 실패: 개봉 중인 영화 조회", e);
            throw new RuntimeException("TMDB API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** tmdb api - 영화 상세 */
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

    /** tmdb api - 영화 모든 장르 */
    public List<TmdbGenreResponseDto.Genre> getGenres() {
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

    /** 응답값 영화 엔티티 변경 */
    public Movie convertToMovie(TmdbMovieResponseDto tmdbMovie) {
        Movie movie = new Movie();
        movie.setTmdbId(tmdbMovie.id());
        movie.setTitle(tmdbMovie.title());
        movie.setReleaseDate(tmdbMovie.releaseDate());
        movie.setPosterUrl(tmdbMovie.posterPath() != null 
                ? tmdbImageBaseUrl + tmdbMovie.posterPath() 
                : null);
        movie.setOverview(tmdbMovie.overview());
        movie.setTmdbRate(tmdbMovie.voteAverage());
        return movie;
    }

    /** 영화 엔티티 저장 or 업데이트 */
    public Movie saveOrUpdateMovie(TmdbMovieResponseDto tmdbMovie) {
        return movieRepository.findByTmdbId(tmdbMovie.id())
                .map(existingMovie -> {
                    existingMovie.setTitle(tmdbMovie.title());
                    existingMovie.setReleaseDate(tmdbMovie.releaseDate());
                    existingMovie.setPosterUrl(tmdbMovie.posterPath() != null 
                            ? tmdbImageBaseUrl + tmdbMovie.posterPath() 
                            : null);
                    existingMovie.setOverview(tmdbMovie.overview());
                    existingMovie.setTmdbRate(tmdbMovie.voteAverage());
                    return movieRepository.save(existingMovie);
                })
                .orElseGet(() -> {
                    Movie newMovie = convertToMovie(tmdbMovie);
                    return movieRepository.save(newMovie);
                });
    }
}

