package org.example.backend.repository.keyword;

import org.example.backend.domain.keyword.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}
