package org.example.backend.service.user;

import java.util.Optional;

import org.example.backend.domain.user.User;
import org.example.backend.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /** 사용자 조회 */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /** 이메일로 사용자 조회 */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /** 닉네임으로 사용자 조회 */
    @Transactional(readOnly = true)
    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    /** 사용자 저장 */
    public User save(User user) {
        return userRepository.save(user);
    }

    /** 닉네임 수정 */
    public User updateNickname(Long userId, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new RuntimeException("닉네임 빈값입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        userRepository.findByNickname(nickname)
                .filter(other -> !other.getUserId().equals(userId))
                .ifPresent(other -> {
                    throw new RuntimeException("이미 사용 중인 닉네임입니다.");
                });

        User updated = user.toBuilder()
                .nickname(nickname)
                .build();
        return userRepository.save(updated);
    }
}
