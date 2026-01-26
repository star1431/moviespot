package org.example.backend.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.backend.domain.genre.Genre;

public record TmdbGenreDto(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name
) {
    public Genre toGenre() {
        return Genre.builder()
                .tmdbGenreId(this.id)
                .name(this.name)
                .build();
    }
    
    public static TmdbGenreDto from(Genre genre) {
        return new TmdbGenreDto(
                genre.getTmdbGenreId(),
                genre.getName()
        );
    }
}

