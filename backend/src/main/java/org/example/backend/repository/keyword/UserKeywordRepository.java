package org.example.backend.repository.keyword;

import org.example.backend.domain.keyword.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
}
