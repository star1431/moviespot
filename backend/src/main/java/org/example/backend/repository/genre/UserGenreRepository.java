package org.example.backend.repository.genre;

import org.example.backend.domain.genre.UserGenre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
}
