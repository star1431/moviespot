package org.example.backend.service.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.genre.Genre;
import org.example.backend.domain.genre.UserGenre;
import org.example.backend.domain.user.User;
import org.example.backend.dto.genre.GenreResponseDto;
import org.example.backend.external.tmdb.client.TmdbClient;
import org.example.backend.external.tmdb.dto.TmdbGenreDto;
import org.example.backend.repository.genre.GenreRepository;
import org.example.backend.repository.genre.UserGenreRepository;
import org.example.backend.repository.user.UserRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

    private final TmdbClient tmdbClient;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;
    private final ApplicationContext applicationContext;

    /** 셀프 인젝션 처리 */
    private GenreService getSelf() {
        return applicationContext.getBean(GenreService.class);
    }

    /** 장르 목록 조회  */
    @Transactional(readOnly = true)
    public List<TmdbGenreDto> getGenres() {
        List<Genre> dbGenres = genreRepository.findAll();
        if (!dbGenres.isEmpty()) {
            // 있으면 db 그대로 사용
            return dbGenres.stream()
                    .map(TmdbGenreDto::from)
                    .collect(Collectors.toList());
        }

        return getSelf().fetchGenresFromApiAndSave();
    }

    /** tmdb api 장르 목록 가지고와서 저장 (별도 쓰기 트랜잭션) */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<TmdbGenreDto> fetchGenresFromApiAndSave() {
        List<TmdbGenreDto> genres = tmdbClient.fetchGenres();

        if (!genres.isEmpty()) {
            genres.forEach(this::saveOrUpdateGenre);
            log.info("장르 데이터 저장 완료: {}개", genres.size());
        }

        return genres;
    }

    /** 장르 db저장 및 업데이트 */
    @Transactional
    public Genre saveOrUpdateGenre(TmdbGenreDto tmdbGenre) {
        return genreRepository.findByTmdbGenreId(tmdbGenre.id())
                .orElseGet(() -> {
                    Genre newGenre = tmdbGenre.toGenre();
                    return genreRepository.save(newGenre);
                });
    }

    /** 내 관심 장르 목록 조회 */
    @Transactional(readOnly = true)
    public List<GenreResponseDto> getMyGenres(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        return userGenreRepository.findByUser(user).stream()
                .map(ug -> new GenreResponseDto(ug.getGenre().getTmdbGenreId(), ug.getGenre().getName()))
                .collect(Collectors.toList());
    }

    /** 내 관심 장르 등록 */
    @Transactional
    public void createMyGenre(Long userId, Long tmdbGenreId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        Genre genre = genreRepository.findByTmdbGenreId(tmdbGenreId)
                .orElseThrow(() -> new RuntimeException("장르를 찾을 수 없습니다: " + tmdbGenreId));

        boolean exists = userGenreRepository.findByUserUserIdAndGenreTmdbGenreId(userId, tmdbGenreId).isPresent();
        if (exists) {
            return;
        }

        UserGenre userGenre = UserGenre.builder()
                .user(user)
                .genre(genre)
                .build();
        userGenreRepository.save(userGenre);
    }

    /** 내 관심 장르 삭제 */
    @Transactional
    public void deleteMyGenre(Long userId, Long tmdbGenreId) {
        userGenreRepository.deleteByUserUserIdAndGenreTmdbGenreId(userId, tmdbGenreId);
    }
}
