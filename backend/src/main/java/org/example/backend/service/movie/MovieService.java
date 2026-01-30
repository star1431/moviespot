package org.example.backend.service.movie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.genre.MovieGenre;
import org.example.backend.domain.movie.Movie;
import org.example.backend.domain.movie.MovieType;
import org.example.backend.domain.movie.UserRating;
import org.example.backend.domain.movie.WatchedMovie;
import org.example.backend.domain.user.User;
import org.example.backend.dto.common.SliceResponseDto;
import org.example.backend.dto.genre.GenreResponseDto;
import org.example.backend.dto.movie.MovieDetailResponseDto;
import org.example.backend.dto.movie.MovieUserRatingResponseDto;
import org.example.backend.dto.movie.MovieResponseDto;
import org.example.backend.dto.movie.MovieUserRatingUpsertRequestDto;
import org.example.backend.external.tmdb.client.TmdbClient;
import org.example.backend.external.tmdb.dto.TmdbMovieResponseDto;
import org.example.backend.external.tmdb.dto.TmdbVideoDto;
import org.example.backend.external.tmdb.mapper.TmdbMovieMapper;
import org.example.backend.repository.genre.GenreRepository;
import org.example.backend.repository.movie.MovieRepository;
import org.example.backend.repository.movie.WatchedMovieRepository;
import org.example.backend.repository.review.UserRatingRepository;
import org.example.backend.repository.user.UserRepository;
import org.example.backend.repository.genre.MovieGenreRepository;
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;
import java.util.HashMap;
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
    private final MovieGenreRepository movieGenreRepository;
    private final GenreRepository genreRepository;
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
    // @Transactional(readOnly = true) // 내부에 별도로 쓰기트랜잭션 있지만, 읽기 당시 이전 스냅샷으로 고정되서 제거
    public Slice<MovieResponseDto> getTopRatedMovies(Pageable pageable) {
        LocalDateTime todayMidnight = getTodayMidnight();
        long count = movieRepository.countByMovieTypeAndUpdatedAtGreaterThanEqual(MovieType.POPULAR, todayMidnight);
        
        if (count < INITIAL_MOVIE_COUNT) {
            log.info("전체 인기작: 최신 반영 로직 진행");
            getSelf().updateTopRatedMovies();
        }
        
        return getMoviesFromDb(MovieType.POPULAR, todayMidnight, pageable);
    }

    /** 인기 상영작 목록 조회 */
    // @Transactional(readOnly = true) // 내부에 별도로 쓰기트랜잭션 있지만, 읽기 당시 이전 스냅샷으로 고정되서 제거
    public Slice<MovieResponseDto> getNowPlayingMovies(Pageable pageable) {
        LocalDateTime todayMidnight = getTodayMidnight();
        long count = movieRepository.countByMovieTypeAndUpdatedAtGreaterThanEqual(MovieType.NOW_PLAYING, todayMidnight);
        
        if (count < INITIAL_MOVIE_COUNT) {
            log.info("인기 상영작: 최신 반영 로직 진행");
            getSelf().updateNowPlayingMovies();
        }
        
        return getMoviesFromDb(MovieType.NOW_PLAYING, todayMidnight, pageable);
    }

    /** 영화 상세 조회 */
    @Transactional(readOnly = true)
    public MovieDetailResponseDto getMovieDetail(Long tmdbId, Long userId, int commentPage, int commentSize) {
        // DB에서 영화 조회
        Optional<Movie> movieOpt = movieRepository.findByTmdbId(tmdbId);
        
        if (movieOpt.isPresent()) {
            // DB에 있으면 DB 데이터만 사용 (TMDB로 보강/업데이트 하지 않음)
            return getMovieDetailFromDb(movieOpt.get(), userId, commentPage, commentSize);
        } else {
            // DB에 없으면 TMDB API 호출 후 DB에 저장
            return getMovieDetailFromTmdb(tmdbId, userId, commentPage, commentSize);
        }
    }

    /** DB에서 영화 상세 정보 조회 */
    private MovieDetailResponseDto getMovieDetailFromDb(Movie movie, Long userId, int commentPage, int commentSize) {
        // 우리회원 평균 평점 계산
        Double userAverageRating = userRatingRepository
                .calculateAverageRatingByTmdbId(movie.getTmdbId())
                .orElse(null);

        int safePage = Math.max(0, commentPage);
        int safeSize = Math.max(1, Math.min(commentSize, 50));

        List<UserRating> fetched = userRatingRepository
                .findByMovieTmdbIdOrderByCreatedAtDesc(movie.getTmdbId(), PageRequest.of(safePage, safeSize + 1));

        boolean hasNext = fetched.size() > safeSize;
        List<UserRating> pageContent = hasNext ? fetched.subList(0, safeSize) : fetched;

        List<MovieUserRatingResponseDto> ratingDtos = pageContent.stream()
                .map(r -> new MovieUserRatingResponseDto(
                        r.getUser().getUserId(),
                        r.getUser().getNickname(),
                        r.getScore(),
                        r.getComment(),
                        r.isRecommended(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());

        SliceResponseDto<MovieUserRatingResponseDto> userRatings =
                new SliceResponseDto<>(ratingDtos, safePage, safeSize, hasNext);

        // 장르 ID/이름 목록 가져오기 (DB 기준)
        List<Long> genreIds = movieGenreRepository.findByMovie(movie).stream()
                .map(mg -> mg.getGenre().getTmdbGenreId())
                .collect(Collectors.toList());
        List<GenreResponseDto> genres = movieGenreRepository.findByMovie(movie).stream()
                .map(mg -> new GenreResponseDto(mg.getGenre().getTmdbGenreId(), mg.getGenre().getName()))
                .collect(Collectors.toList());

        if (userId == null) {
            return new MovieDetailResponseDto(
                    movie.getTmdbId(),
                    movie.getTitle(),
                    movie.getReleaseDate(),
                    movie.getPosterUrl(),
                    movie.getOverview(),
                    movie.getTmdbRate(),
                    null, // voteCount는 db에 없음
                    movie.getRuntime(),
                    genreIds,
                    genres,
                    userAverageRating,
                    movie.getTrailerUrl(),
                    userRatings,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null
            );
        }

        Optional<UserRating> userRatingOpt = userRatingRepository
                .findByUserUserIdAndMovieTmdbId(userId, movie.getTmdbId());

        Optional<WatchedMovie> watchedMovieOpt = watchedMovieRepository
                .findByUserUserIdAndMovieTmdbId(userId, movie.getTmdbId());

        return new MovieDetailResponseDto(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getOverview(),
                movie.getTmdbRate(),
                null, // voteCount는 db에 없음
                movie.getRuntime(),
                genreIds,
                genres,
                userAverageRating,
                movie.getTrailerUrl(),
                userRatings,
                userRatingOpt.map(UserRating::getCreatedAt).orElse(null),
                userRatingOpt.map(UserRating::getScore).orElse(null),
                userRatingOpt.map(UserRating::getComment).orElse(null),
                userRatingOpt.map(UserRating::isRecommended).orElse(null),
                watchedMovieOpt.isPresent(),
                watchedMovieOpt.map(WatchedMovie::getScore).orElse(null)
        );
    }

    /** Tmdb api에서 영화 상세 정보 조회 */
    @Transactional(readOnly = true)
    private MovieDetailResponseDto getMovieDetailFromTmdb(Long tmdbId, Long userId, int commentPage, int commentSize) {
        TmdbMovieResponseDto tmdbMovie = tmdbClient.fetchMovieDetail(tmdbId);
        
        // 트레일러 
        String trailerUrl = pickTrailerUrl(tmdbClient.fetchMovieVideos(tmdbId));
        
        // 우리회원 평균 평점 계산
        Double userAverageRating = userRatingRepository
                .calculateAverageRatingByTmdbId(tmdbId)
                .orElse(null);

        int safePage = Math.max(0, commentPage);
        int safeSize = Math.max(1, Math.min(commentSize, 50));

        List<UserRating> fetched = userRatingRepository
                .findByMovieTmdbIdOrderByCreatedAtDesc(tmdbId, PageRequest.of(safePage, safeSize + 1));

        boolean hasNext = fetched.size() > safeSize;
        List<UserRating> pageContent = hasNext ? fetched.subList(0, safeSize) : fetched;

        List<MovieUserRatingResponseDto> ratingDtos = pageContent.stream()
                .map(r -> new MovieUserRatingResponseDto(
                        r.getUser().getUserId(),
                        r.getUser().getNickname(),
                        r.getScore(),
                        r.getComment(),
                        r.isRecommended(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());

        SliceResponseDto<MovieUserRatingResponseDto> userRatings =
                new SliceResponseDto<>(ratingDtos, safePage, safeSize, hasNext);

        // 포스터 URL 생성
        String posterUrl = tmdbMovie.posterPath() != null
                ? tmdbImageBaseUrl + tmdbMovie.posterPath()
                : null;

        List<Long> genreIds = tmdbMovie.effectiveGenreIds();
        List<GenreResponseDto> genres = (tmdbMovie.genres() == null) ? List.of()
                : tmdbMovie.genres().stream()
                .map(g -> new GenreResponseDto(g.id(), g.name()))
                .collect(Collectors.toList());

        if (userId == null) {
            return new MovieDetailResponseDto(
                    tmdbMovie.id(),
                    tmdbMovie.title(),
                    tmdbMovie.releaseDate(),
                    posterUrl,
                    tmdbMovie.overview(),
                    tmdbMovie.voteAverage(),
                    tmdbMovie.voteCount(),
                    tmdbMovie.runtime(),
                    genreIds,
                    genres,
                    userAverageRating,
                    trailerUrl,
                    userRatings,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null
            );
        }

        Optional<UserRating> userRatingOpt = userRatingRepository
                .findByUserUserIdAndMovieTmdbId(userId, tmdbId);

        Optional<WatchedMovie> watchedMovieOpt = watchedMovieRepository
                .findByUserUserIdAndMovieTmdbId(userId, tmdbId);

        return new MovieDetailResponseDto(
                tmdbMovie.id(),
                tmdbMovie.title(),
                tmdbMovie.releaseDate(),
                posterUrl,
                tmdbMovie.overview(),
                tmdbMovie.voteAverage(),
                tmdbMovie.voteCount(),
                tmdbMovie.runtime(),
                genreIds,
                genres,
                userAverageRating,
                trailerUrl,
                userRatings,
                userRatingOpt.map(UserRating::getCreatedAt).orElse(null),
                userRatingOpt.map(UserRating::getScore).orElse(null),
                userRatingOpt.map(UserRating::getComment).orElse(null),
                userRatingOpt.map(UserRating::isRecommended).orElse(null),
                watchedMovieOpt.isPresent(),
                watchedMovieOpt.map(WatchedMovie::getScore).orElse(null)
        );
    }

    /** 사용자 평점/코멘트 등록/수정 */
    @Transactional
    public void upsertUserRating(Long userId, MovieUserRatingUpsertRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        // 영화 조회 또는 생성
        Movie movie = getOrCreateMovieByTmdbId(requestDto.tmdbId());
        
        Optional<UserRating> existingRatingOpt = userRatingRepository
                .findByUserUserIdAndMovieTmdbId(userId, requestDto.tmdbId());
        
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
            log.info("사용자 평점 수정 완료: userId={}, tmdbId={}", userId, requestDto.tmdbId());
        } else {
            UserRating newRating = UserRating.builder()
                    .score(requestDto.score())
                    .comment(requestDto.comment())
                    .isRecommended(requestDto.isRecommended())
                    .user(user)
                    .movie(movie)
                    .build();
            userRatingRepository.save(newRating);
            log.info("사용자 평점 등록 완료: userId={}, tmdbId={}", userId, requestDto.tmdbId());
        }
    }

    /** 사용자 평점/코멘트 삭제 */
    @Transactional
    public void deleteUserRating(Long userId, Long tmdbId) {
        userRatingRepository.deleteByUserUserIdAndMovieTmdbId(userId, tmdbId);
        log.info("사용자 평점 삭제 완료: userId={}, tmdbId={}", userId, tmdbId);
    }

    /** 영화 찾기 (제목, 장르, 연도, 정렬 필터링) */
    @Transactional(readOnly = true)
    public Slice<MovieResponseDto> getMovies(
            String keyword,
            Long genreId,
            Integer releaseYearFrom,
            Integer releaseYearTo,
            String sortBy,
            Pageable pageable) {
        // 정렬 옵션 검증 및 기본값 설정
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "latest";
        }
        
        Slice<TmdbMovieResponseDto> tmdbMovies;
        
        // 제목 검색인 경우 search api 사용 
        if (keyword != null && keyword.length() >= 2) {
            tmdbMovies = tmdbClient.searchMovies(keyword, pageable);
        } else {
            // discover api 사용 (장르, 연도 범위, 정렬 필터링)
            tmdbMovies = tmdbClient.discoverMovies(pageable, genreId, releaseYearFrom, releaseYearTo, sortBy);
        }
        
        return convertTmdbMoviesToResponseDto(tmdbMovies, pageable);
    }

    /**  전체 인기작 목록 조회  (tmdb api 그대로) */
    @Transactional(readOnly = true)
    public Slice<MovieResponseDto> getTopRatedMoviesList(Pageable pageable) {
        Slice<TmdbMovieResponseDto> tmdbMovies = tmdbClient.fetchTopRatedMovies(pageable);
        return convertTmdbMoviesToResponseDto(tmdbMovies, pageable);
    }

    /** 인기 상영작 목록 조회 (tmdb api 그대로) */
    @Transactional(readOnly = true)
    public Slice<MovieResponseDto> getNowPlayingMoviesList(Pageable pageable) {
        Slice<TmdbMovieResponseDto> tmdbMovies = tmdbClient.fetchNowPlayingMovies(pageable);
        return convertTmdbMoviesToResponseDto(tmdbMovies, pageable);
    }

    /** 개봉 예정 영화 목록 조회 (tmdb api 그대로) */
    @Transactional(readOnly = true)
    public Slice<MovieResponseDto> getUpcomingMoviesList(Pageable pageable) {
        Slice<TmdbMovieResponseDto> tmdbMovies = tmdbClient.fetchUpcomingMovies(pageable);
        return convertTmdbMoviesToResponseDto(tmdbMovies, pageable);
    }

    /** tmdb api 응답객체 -> MovieResponseDto 변환 (공통) */
    private Slice<MovieResponseDto> convertTmdbMoviesToResponseDto(
            Slice<TmdbMovieResponseDto> tmdbMovies, Pageable pageable) {
        List<TmdbMovieResponseDto> tmdbMovieList = tmdbMovies.getContent();
        
        // 우리회원 평균 평점 계산 (DB에 있는 영화만)
        List<Long> tmdbIds = tmdbMovieList.stream()
                .map(TmdbMovieResponseDto::id)
                .collect(Collectors.toList());
        
        Map<Long, Double> averageRatingMap = new HashMap<>();
        if (!tmdbIds.isEmpty()) {
            List<Object[]> ratingResults = userRatingRepository.calculateAverageRatingsByTmdbIds(tmdbIds);
            for (Object[] result : ratingResults) {
                Long tmdbId = (Long) result[0];
                Double avgRating = (Double) result[1];
                averageRatingMap.put(tmdbId, avgRating);
            }
        }
        
        final Map<Long, Double> finalRatingMap = averageRatingMap;
        List<MovieResponseDto> content = tmdbMovieList.stream()
                .map(tmdbMovie -> {
                    String posterUrl = tmdbMovie.posterPath() != null
                            ? tmdbImageBaseUrl + tmdbMovie.posterPath()
                            : null;
                    return new MovieResponseDto(
                            tmdbMovie.id(),
                            tmdbMovie.title(),
                            tmdbMovie.releaseDate(),
                            posterUrl,
                            tmdbMovie.voteAverage(),
                            finalRatingMap.get(tmdbMovie.id()),
                            tmdbMovie.genreIds() != null ? tmdbMovie.genreIds() : List.of()
                    );
                })
                .collect(Collectors.toList());
        
        return new SliceImpl<>(content, pageable, tmdbMovies.hasNext());
    }

    // 내부 처리 ----------------

    /** DB에서 영화 목록 조회 */
    private Slice<MovieResponseDto> getMoviesFromDb(MovieType movieType, LocalDateTime todayMidnight, Pageable pageable) {
        List<Movie> movies = movieRepository.findByMovieTypeOrderByCreatedAtDesc(movieType, todayMidnight, pageable);
        
        // 조회로 평균 평점 한 번에 계산
        List<Long> tmdbIds = movies.stream()
                .map(Movie::getTmdbId)
                .collect(Collectors.toList());
        
        Map<Long, Double> averageRatingMap = new HashMap<>();
        if (!tmdbIds.isEmpty()) {
            List<Object[]> ratingResults = userRatingRepository.calculateAverageRatingsByTmdbIds(tmdbIds);
            for (Object[] result : ratingResults) {
                Long tmdbId = (Long) result[0];
                Double avgRating = (Double) result[1];
                averageRatingMap.put(tmdbId, avgRating);
            }
        }
        
        final Map<Long, Double> finalRatingMap = averageRatingMap;
        List<MovieResponseDto> content = movies.stream()
                .map(movie -> toMovieResponseDto(movie, finalRatingMap.get(movie.getTmdbId())))
                .collect(Collectors.toList());
        
        boolean hasNext = content.size() == pageable.getPageSize();
        return new SliceImpl<>(content, pageable, hasNext);
    }
    
    /** movie -> dto */
    private MovieResponseDto toMovieResponseDto(Movie movie, Double userAverageRating) {
        // 장르 ID 목록 (DB에 저장된 movie_genre 관계 기반)
        List<Long> genreIds = movieGenreRepository.findByMovie(movie).stream()
                .map(mg -> mg.getGenre().getTmdbGenreId())
                .collect(Collectors.toList());

        return new MovieResponseDto(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getTmdbRate(),
                userAverageRating,
                genreIds
        );
    }

    /** 전체 인기작 데이터 업데이트 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTopRatedMovies() {
        Pageable firstPage = PageRequest.of(0, INITIAL_MOVIE_COUNT);
        Slice<TmdbMovieResponseDto> apiMovies = tmdbClient.fetchTopRatedMovies(firstPage);
        List<TmdbMovieResponseDto> apiMovieList = apiMovies.getContent();
        
        updateMoviesWithComparison(apiMovieList, MovieType.POPULAR);
        
        log.info("전체 인기작 데이터 업데이트 완료 (Top Rated): {}개", apiMovieList.size());
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
        
        List<Movie> dbMovies = movieRepository.findByMovieType(movieType);
        Set<Long> apiTmdbIds = apiMovies.stream()
                .map(TmdbMovieResponseDto::id)
                .collect(Collectors.toSet());

        // db에 있음 | tmdb api 없음 = 해당 타입만 제거 (다른 타입은 유지)
        for (Movie dbMovie : dbMovies) {
            if (!apiTmdbIds.contains(dbMovie.getTmdbId())) {
                Set<MovieType> types = normalizeTypes(dbMovie.getMovieTypes());
                types.remove(movieType);
                Movie updatedMovie = dbMovie.toBuilder()
                        .movieTypes(types.isEmpty() ? null : types)
                        .updatedAt(types.isEmpty() ? null : dbMovie.getUpdatedAt())
                        .build();
                movieRepository.save(updatedMovie);
            }
        }

        // api 목록 순서대로 갱신
        for (TmdbMovieResponseDto apiMovie : apiMovies) {
            Movie movie = saveOrUpdateMovieBase(apiMovie);
            Set<MovieType> types = normalizeTypes(movie.getMovieTypes());
            types.add(movieType);

            // 트레일러 URL이 없으면 가져와서 저장
            String trailerUrl = movie.getTrailerUrl();
            if (trailerUrl == null || trailerUrl.isBlank()) {
                try {
                    String pickedUrl = pickTrailerUrl(tmdbClient.fetchMovieVideos(apiMovie.id()));
                    if (pickedUrl != null && !pickedUrl.isBlank()) {
                        trailerUrl = pickedUrl;
                    }
                } catch (Exception e) {
                    log.warn("트레일러 URL 가져오기 실패: tmdbId={}, error={}", apiMovie.id(), e.getMessage());
                }
            }

            Movie updatedMovie = movie.toBuilder()
                    .movieTypes(types)
                    .updatedAt(todayMidnight)
                    .trailerUrl(trailerUrl)
                    .build();
            movieRepository.save(updatedMovie);
        }
    }

    private Set<MovieType> normalizeTypes(Set<MovieType> current) {
        if (current == null || current.isEmpty()) {
            return EnumSet.noneOf(MovieType.class);
        }
        return EnumSet.copyOf(current);
    }

    /** 영화 상세용 대표 트레일러 URL 선택 (유튜브 만) */
    private String pickTrailerUrl(List<TmdbVideoDto> videos) {
        if (videos == null || videos.isEmpty()) {
            return null;
        }

        Comparator<TmdbVideoDto> score = Comparator
                // Trailer 우선
                .comparing((TmdbVideoDto v) -> "Trailer".equalsIgnoreCase(v.type()) ? 0 : 1)
                // 공식 우선
                .thenComparing(v -> Boolean.TRUE.equals(v.official()) ? 0 : 1)
                // YouTube 우선
                .thenComparing(v -> "YouTube".equalsIgnoreCase(v.site()) ? 0 : 1);

        TmdbVideoDto best = videos.stream()
                .filter(v -> v.site() != null && v.key() != null)
                .min(score)
                .orElse(null);

        if (best == null) {
            return null;
        }

        String youtubeUrl = "YouTube".equalsIgnoreCase(best.site())
                ? "https://www.youtube.com/watch?v=" + best.key()
                : null;

        return youtubeUrl;
    }

    /** 트레일러 url db에 있으면 사용  | 없으면 받아옴 */
    @Transactional
    public String getOrCreateFixedTrailerUrl(Long tmdbId) {
        Movie movie = movieRepository.findByTmdbId(tmdbId).orElse(null);

        if (movie != null && movie.getTrailerUrl() != null && !movie.getTrailerUrl().isBlank()) {
            return movie.getTrailerUrl();
        }

        String pickedUrl = pickTrailerUrl(tmdbClient.fetchMovieVideos(tmdbId));
        if (pickedUrl == null) {
            return null;
        }

        if (movie != null) {
            Movie updated = movie.toBuilder()
                    .trailerUrl(pickedUrl)
                    .build();
            movieRepository.save(updated);
        }

        return pickedUrl;
    }

    /** 영화 기본정보 저장/업데이트(tmdb 기준) */
    @Transactional
    public Movie saveOrUpdateMovieBase(TmdbMovieResponseDto tmdbMovie) {
        Movie saved = movieRepository.findByTmdbId(tmdbMovie.id())
                .map(existingMovie -> {
                    // 트레일러 URL이 없으면 가져와서 저장
                    String trailerUrl = existingMovie.getTrailerUrl();
                    if (trailerUrl == null || trailerUrl.isBlank()) {
                        try {
                            String pickedUrl = pickTrailerUrl(tmdbClient.fetchMovieVideos(tmdbMovie.id()));
                            if (pickedUrl != null && !pickedUrl.isBlank()) {
                                trailerUrl = pickedUrl;
                            }
                        } catch (Exception e) {
                            log.warn("트레일러 URL 가져오기 실패: tmdbId={}, error={}", tmdbMovie.id(), e.getMessage());
                        }
                    }

                    Movie updatedMovie = existingMovie.toBuilder()
                            .title(tmdbMovie.title())
                            .releaseDate(tmdbMovie.releaseDate())
                            .posterUrl(tmdbMovie.posterPath() != null
                                    ? tmdbImageBaseUrl + tmdbMovie.posterPath()
                                    : null)
                            .overview(tmdbMovie.overview())
                            .tmdbRate(tmdbMovie.voteAverage())
                            .runtime(tmdbMovie.runtime())
                            .trailerUrl(trailerUrl)
                            .build();
                    return movieRepository.save(updatedMovie);
                })
                .orElseGet(() -> {
                    Movie newMovie = tmdbMovieMapper.toMovie(tmdbMovie);
                    
                    // 새 영화 저장 시 트레일러 URL도 가져와서 저장
                    try {
                        String pickedUrl = pickTrailerUrl(tmdbClient.fetchMovieVideos(tmdbMovie.id()));
                        if (pickedUrl != null && !pickedUrl.isBlank()) {
                            newMovie = newMovie.toBuilder()
                                    .trailerUrl(pickedUrl)
                                    .build();
                        }
                    } catch (Exception e) {
                        log.warn("트레일러 URL 가져오기 실패: tmdbId={}, error={}", tmdbMovie.id(), e.getMessage());
                    }
                    
                    return movieRepository.save(newMovie);
                });

        
        attachMovieGenreSave(saved, tmdbMovie);
        return saved;
    }

    private void attachMovieGenreSave(Movie movie, TmdbMovieResponseDto tmdbMovie) {
        if (movie == null || tmdbMovie == null) return;

        List<Long> tmdbGenreIds = tmdbMovie.effectiveGenreIds();
        if (tmdbGenreIds == null || tmdbGenreIds.isEmpty()) return;

        Set<Long> existing = movieGenreRepository.findByMovie(movie).stream()
                .map(mg -> mg.getGenre().getTmdbGenreId())
                .collect(Collectors.toSet());

        if (existing == null) existing = new HashSet<>();

        for (Long tmdbGenreId : tmdbGenreIds) {
            if (tmdbGenreId == null) continue;
            if (existing.contains(tmdbGenreId)) continue;

            genreRepository.findByTmdbGenreId(tmdbGenreId).ifPresent(genre -> {
                movieGenreRepository.save(
                        MovieGenre.builder()
                                .movie(movie)
                                .genre(genre)
                                .build()
                );
            });
        }
    }

    /** tmdbId로 영화 조회 또는 생성 (공통) */
    @Transactional
    public Movie getOrCreateMovieByTmdbId(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> {
                    log.info("영화가 DB에 없어서 자동 생성: tmdbId={}", tmdbId);
                    TmdbMovieResponseDto tmdbMovie = tmdbClient.fetchMovieDetail(tmdbId);
                    return saveOrUpdateMovieBase(tmdbMovie);
                });
    }
}
