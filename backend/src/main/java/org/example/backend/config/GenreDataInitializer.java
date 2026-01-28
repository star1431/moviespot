package org.example.backend.config;

import java.util.List;

import org.example.backend.domain.genre.Genre;
import org.example.backend.external.tmdb.client.TmdbClient;
import org.example.backend.external.tmdb.dto.TmdbGenreDto;
import org.example.backend.repository.genre.GenreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenreDataInitializer implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final TmdbClient tmdbClient;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 장르 데이터가 있으면 스킵
        if (!genreRepository.findAll().isEmpty()) {
            log.info("장르 데이터 이미 존재");
            return;
        }

        try {
            // tmdb api에서 장르 목록 가져오기
            List<TmdbGenreDto> genres = tmdbClient.fetchGenres();
            
            if (genres.isEmpty()) {
                log.warn("tmdb api 연결실패");
                return;
            }

            // 장르 데이터 저장
            genres.forEach(tmdbGenre -> {
                Genre genre = Genre.builder()
                        .tmdbGenreId(tmdbGenre.id())
                        .name(tmdbGenre.name())
                        .build();
                genreRepository.save(genre);
            });

            log.info("장르 데이터 저장 완료: {}개", genres.size());
        } catch (Exception e) {
            log.error("장르 데이터 저장 중 오류 발생", e);
        }
    }
}

