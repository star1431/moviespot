package org.example.backend.domain.movie;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Movie {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long movieId;

    @Column(nullable = false, unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    private String releaseDate;

    private String posterUrl;

    @Column(columnDefinition = "TEXT")
    private String overview;

    private Float tmdbRate;

    // 인기상영, 전체인기 등 타입 복수 존재해서 조인테이블 추가
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "movie_types",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "movie_type", nullable = false)
    private Set<MovieType> movieTypes;

    /** 대표 트레일러(고정) - 유튜브 링크 URL 1개만 저장 */
    @Column(name = "trailer_url", columnDefinition = "TEXT")
    private String trailerUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;
}
