package org.example.backend.service.movie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.movie.MovieType;
import org.example.backend.domain.movie.UserRating;
import org.example.backend.domain.movie.WatchedMovie;
import org.example.backend.domain.user.User;
import org.example.backend.dto.movie.MovieDetailDto;
import org.example.backend.dto.movie.MovieResponseDto;
import org.example.backend.dto.movie.UserRatingRequestDto;
import org.example.backend.external.tmdb.client.TmdbClient;
import org.example.backend.external.tmdb.dto.TmdbMovieResponseDto;
import org.example.backend.external.tmdb.mapper.TmdbMovieMapper;
import org.example.backend.repository.movie.MovieRepository;
import org.example.backend.repository.movie.WatchedMovieRepository;
import org.example.backend.repository.review.UserRatingRepository;
import org.example.backend.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private static final int INITIAL_MOVIE_COUNT = 10;

    private final TmdbClient tmdbClient;
    private final TmdbMovieMapper tmdbMovieMapper;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final UserRatingRepository userRatingRepository;
    private final WatchedMovieRepository watchedMovieRepository;
    private final ApplicationContext applicationContext;

    @Value("${tmdb.api.image-base-url}")
    private String tmdbImageBaseUrl;

    /** 셀프 인젝션 처리 */
    private MovieService getSelf() {
        return applicationContext.getBean(MovieService.class);
    }

    /** 오늘 자정 시간 반환 */
    private LocalDateTime getTodayMidnight() {
        return LocalDate.now().atTime(LocalTime.MIN);
    }

    /** 전체 인기작 목록 조회 */
    @Transactional(readOnly = true)
    public Slice<MovieResponseDto> getPopularMovies(Pageable pageable) {
        LocalDateTime todayMidnight = getTodayMidnight();
        long count = movieRepository.countByMovieTypeAndUpdatedAtAfter(MovieType.POPULAR, todayMidnight);
        
        if (count < INITIAL_MOVIE_COUNT) {
            log.info("전체 인기작: 최신 반영 로직 진행");
            getSelf().updatePopularMovies();
        }
        
        return getMoviesFromDb(MovieType.POPULAR, pageable);
    }

    /** 인기 상영작 목록 조회 */
    @Transactional(readOnly = true)
    public Slice<MovieResponseDto> getNowPlayingMovies(Pageable pageable) {
        LocalDateTime todayMidnight = getTodayMidnight();
        long count = movieRepository.countByMovieTypeAndUpdatedAtAfter(MovieType.NOW_PLAYING, todayMidnight);
        
        if (count < INITIAL_MOVIE_COUNT) {
            log.info("인기 상영작: 최신 반영 로직 진행");
            getSelf().updateNowPlayingMovies();
        }
        
        return getMoviesFromDb(MovieType.NOW_PLAYING, pageable);
    }

    /** 영화 상세 조회 (사용자 정보 포함) */
    @Transactional(readOnly = true)
    public MovieDetailDto getMovieDetail(Long tmdbId, Long userId) {
        TmdbMovieResponseDto tmdbMovie = tmdbClient.fetchMovieDetail(tmdbId);
        
        Optional<UserRating> userRatingOpt = userRatingRepository
                .findByUserUserIdAndMovieTmdbId(userId, tmdbId);
        
        Optional<WatchedMovie> watchedMovieOpt = watchedMovieRepository
                .findByUserUserIdAndMovieTmdbId(userId, tmdbId);
        
        return new MovieDetailDto(
                tmdbMovie.id(),
                tmdbMovie.title(),
                tmdbMovie.releaseDate(),
                tmdbMovie.posterPath(),
                tmdbMovie.overview(),
                tmdbMovie.voteAverage(),
                userRatingOpt.map(UserRating::getCreatedAt).orElse(null),
                userRatingOpt.map(UserRating::getScore).orElse(null),
                userRatingOpt.map(UserRating::getComment).orElse(null),
                userRatingOpt.map(UserRating::isRecommended).orElse(null),
                watchedMovieOpt.isPresent(),
                watchedMovieOpt.map(WatchedMovie::getScore).orElse(null)
        );
    }

    /** 영화 상세 조회 (단순 api) */
    @Transactional(readOnly = true)
    public TmdbMovieResponseDto getMovieDetail(Long tmdbId) {
        return tmdbClient.fetchMovieDetail(tmdbId);
    }

    /** 사용자 평점/코멘트 저장 */
    @Transactional
    public void saveUserRating(Long userId, Long tmdbId, UserRatingRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        Movie movie = movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> {
                    TmdbMovieResponseDto tmdbMovie = tmdbClient.fetchMovieDetail(tmdbId);
                    Movie newMovie = tmdbMovieMapper.toMovie(tmdbMovie);
                    return movieRepository.save(newMovie);
                });
        
        Optional<UserRating> existingRatingOpt = userRatingRepository
                .findByUserUserIdAndMovieTmdbId(userId, tmdbId);
        
        if (existingRatingOpt.isPresent()) {
            UserRating existingRating = existingRatingOpt.get();
            UserRating updatedRating = UserRating.builder()
                    .userRatingId(existingRating.getUserRatingId())
                    .score(requestDto.score())
                    .comment(requestDto.comment())
                    .isRecommended(requestDto.isRecommended())
                    .user(existingRating.getUser())
                    .movie(existingRating.getMovie())
                    .createdAt(existingRating.getCreatedAt())
                    .build();
            userRatingRepository.save(updatedRating);
        } else {
            UserRating newRating = UserRating.builder()
                    .score(requestDto.score())
                    .comment(requestDto.comment())
                    .isRecommended(requestDto.isRecommended())
                    .user(user)
                    .movie(movie)
                    .build();
            userRatingRepository.save(newRating);
        }
        
        log.info("사용자 평점 저장 완료: userId={}, tmdbId={}", userId, tmdbId);
    }

    /** 사용자 평점/코멘트 삭제 */
    @Transactional
    public void deleteUserRating(Long userId, Long tmdbId) {
        userRatingRepository.deleteByUserUserIdAndMovieTmdbId(userId, tmdbId);
        log.info("사용자 평점 삭제 완료: userId={}, tmdbId={}", userId, tmdbId);
    }

    // 내부 처리 ----------------

    /** DB에서 영화 목록 조회 */
    private Slice<MovieResponseDto> getMoviesFromDb(MovieType movieType, Pageable pageable) {
        List<Movie> movies = movieRepository.findByMovieTypeOrderByCreatedAtDesc(movieType, pageable);
        List<MovieResponseDto> content = movies.stream()
                .map(this::toMovieResponseDto)
                .collect(Collectors.toList());
        
        boolean hasNext = content.size() == pageable.getPageSize();
        return new SliceImpl<>(content, pageable, hasNext);
    }
    
    /** Movie 엔티티를 MovieResponseDto로 변환 */
    private MovieResponseDto toMovieResponseDto(Movie movie) {
        return new MovieResponseDto(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getOverview(),
                movie.getTmdbRate()
        );
    }

    /** 전체 인기작 데이터 업데이트 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePopularMovies() {
        Pageable firstPage = PageRequest.of(0, INITIAL_MOVIE_COUNT);
        Slice<TmdbMovieResponseDto> apiMovies = tmdbClient.fetchPopularMovies(firstPage);
        List<TmdbMovieResponseDto> apiMovieList = apiMovies.getContent();
        
        updateMoviesWithComparison(apiMovieList, MovieType.POPULAR);
        
        log.info("전체 인기작 데이터 업데이트 완료: {}개", apiMovieList.size());
    }

    /** 인기 상영작 데이터 업데이트 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateNowPlayingMovies() {
        Pageable firstPage = PageRequest.of(0, INITIAL_MOVIE_COUNT);
        Slice<TmdbMovieResponseDto> apiMovies = tmdbClient.fetchNowPlayingMovies(firstPage);
        List<TmdbMovieResponseDto> apiMovieList = apiMovies.getContent();
        
        updateMoviesWithComparison(apiMovieList, MovieType.NOW_PLAYING);
        
        log.info("인기 상영작 데이터 업데이트 완료: {}개", apiMovieList.size());
    }
    
    /** db 데이터 및 api 데이터 비교 처리 */
    private void updateMoviesWithComparison(List<TmdbMovieResponseDto> apiMovies, MovieType movieType) {
        LocalDateTime todayMidnight = getTodayMidnight();
        
        // DB에서 해당 인기상영, 전체인기 타입 가진 영화만
        List<Movie> dbMovies = movieRepository.findByMovieType(movieType);
        List<Long> dbTmdbIds = dbMovies.stream()
                .map(Movie::getTmdbId)
                .toList();
        
        // tmdb api에서 받은 영화의 tmdbId
        List<Long> apiTmdbIds = apiMovies.stream()
                .map(TmdbMovieResponseDto::id)
                .toList();
        
        // db에 있음 | tmdb api 있음 = updatedAt만 변경
        for (TmdbMovieResponseDto apiMovie : apiMovies) {
            if (dbTmdbIds.contains(apiMovie.id())) {
                Movie existingMovie = movieRepository.findByTmdbId(apiMovie.id())
                        .orElseThrow();
                Movie updatedMovie = existingMovie.toBuilder()
                        .updatedAt(todayMidnight)
                        .build();
                movieRepository.save(updatedMovie);
            }
        }
        
        // db에 없음 | tmdb api 있음 = 저장 (날짜,타입까지)
        for (TmdbMovieResponseDto apiMovie : apiMovies) {
            if (!dbTmdbIds.contains(apiMovie.id())) {
                saveOrUpdateMovie(apiMovie, movieType, todayMidnight);
            }
        }
        
        // db에 있음 | tmdb api 없음 = movieType, updatedAt을 null로 변경
        for (Movie dbMovie : dbMovies) {
            if (!apiTmdbIds.contains(dbMovie.getTmdbId())) {
                Movie updatedMovie = dbMovie.toBuilder()
                        .movieType(null)
                        .updatedAt(null)
                        .build();
                movieRepository.save(updatedMovie);
            }
        }
    }

    /** 영화 저장 */
    @Transactional
    public Movie saveOrUpdateMovie(TmdbMovieResponseDto tmdbMovie, MovieType movieType, LocalDateTime updatedAt) {
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
                            .movieType(movieType)
                            .updatedAt(updatedAt)
                            .build();
                    return movieRepository.save(updatedMovie);
                })
                .orElseGet(() -> {
                    Movie newMovie = tmdbMovieMapper.toMovie(tmdbMovie, movieType);
                    Movie movieWithUpdatedAt = newMovie.toBuilder()
                            .updatedAt(updatedAt)
                            .build();
                    return movieRepository.save(movieWithUpdatedAt);
                });
    }
}
