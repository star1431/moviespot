package org.example.backend.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbVideoDto(
        @JsonProperty("id") String id,
        @JsonProperty("iso_639_1") String iso6391, // 언어
        @JsonProperty("iso_3166_1") String iso31661, // 국가
        @JsonProperty("key") String key,
        @JsonProperty("name") String name,
        @JsonProperty("site") String site,
        @JsonProperty("size") Integer size,
        @JsonProperty("type") String type,
        @JsonProperty("official") Boolean official,
        @JsonProperty("published_at") String publishedAt
) {
}


