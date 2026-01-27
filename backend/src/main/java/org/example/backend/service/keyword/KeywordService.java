package org.example.backend.service.keyword;

import java.util.List;
import java.util.stream.Collectors;

import org.example.backend.domain.keyword.Keyword;
import org.example.backend.domain.keyword.UserKeyword;
import org.example.backend.domain.user.User;
import org.example.backend.dto.keyword.KeywordResponseDto;
import org.example.backend.repository.keyword.KeywordRepository;
import org.example.backend.repository.keyword.UserKeywordRepository;
import org.example.backend.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;

    /** 키워드 전체 목록 조회 */
    @Transactional(readOnly = true)
    public List<KeywordResponseDto> getKeywords() {
        return keywordRepository.findAll().stream()
                .map(k -> new KeywordResponseDto(k.getKeywordId(), k.getName()))
                .collect(Collectors.toList());
    }

    /** 키워드 조회 또는 생성 */
    @Transactional
    public Keyword getOrCreateKeyword(String name) {
        return keywordRepository.findByName(name)
                .orElseGet(() -> keywordRepository.save(Keyword.builder().name(name).build()));
    }

    /** 내 관심 키워드 목록 조회 */
    @Transactional(readOnly = true)
    public List<KeywordResponseDto> getMyKeywords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        return userKeywordRepository.findByUser(user).stream()
                .map(uk -> new KeywordResponseDto(uk.getKeyword().getKeywordId(), uk.getKeyword().getName()))
                .collect(Collectors.toList());
    }

    /** 내 관심 키워드 등록 */
    @Transactional
    public void createMyKeyword(Long userId, String keywordName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        Keyword keyword = getOrCreateKeyword(keywordName);

        boolean exists = userKeywordRepository.findByUserUserIdAndKeywordName(userId, keywordName).isPresent();
        if (exists) {
            return;
        }

        UserKeyword userKeyword = UserKeyword.builder()
                .user(user)
                .keyword(keyword)
                .build();
        userKeywordRepository.save(userKeyword);
    }

    /** 내 관심 키워드 삭제 */
    @Transactional
    public void deleteMyKeyword(Long userId, String keywordName) {
        userKeywordRepository.deleteByUserUserIdAndKeywordName(userId, keywordName);
    }
}
