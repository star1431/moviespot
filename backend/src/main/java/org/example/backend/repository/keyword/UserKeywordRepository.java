package org.example.backend.repository.keyword;

import org.example.backend.domain.keyword.UserKeyword;
import org.example.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findByUser(User user);
    Optional<UserKeyword> findByUserUserIdAndKeywordName(Long userId, String keywordName);
    void deleteByUserUserIdAndKeywordName(Long userId, String keywordName);
}
