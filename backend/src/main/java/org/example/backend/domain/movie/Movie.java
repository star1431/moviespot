package org.example.backend.domain.movie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
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

    private String overview;

    private Float tmdbRate;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
