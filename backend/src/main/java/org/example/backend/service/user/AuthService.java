package org.example.backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.user.RefreshToken;
import org.example.backend.domain.user.User;
import org.example.backend.dto.user.AuthTokensDto;
import org.example.backend.dto.user.LoginRequestDto;
import org.example.backend.dto.user.SignupRequestDto;
import org.example.backend.repository.user.RefreshTokenRepository;
import org.example.backend.repository.user.UserRepository;
import org.example.backend.security.JwtTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtTokenizer jwtTokenizer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    /** 회원가입 */
    @Transactional
    public User signup(SignupRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.findByNickname(requestDto.nickname()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        String encoded = passwordEncoder.encode(requestDto.password());

        User user = User.builder()
                .email(requestDto.email())
                .nickname(requestDto.nickname())
                .password(encoded)
                .provider("local")
                .providerId(requestDto.email())
                .build();

        return userRepository.save(user);
    }

    /** 로그인 */
    @Transactional(readOnly = true)
    public User login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new RuntimeException("존재하지 않은 사용자 입니다."));

        if (user.getPassword() == null) {
            throw new RuntimeException("소셜 로그인 계정입니다. 소셜 로그인으로 진행해주세요.");
        }

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return user;
    }

    /** 토큰 발급 */
    @Transactional
    public AuthTokensDto issueTokens(User user) {
        String accessToken = jwtTokenizer.generateAccessToken(user.getUserId(), user.getEmail());
        String refreshToken = jwtTokenizer.generateRefreshToken(user.getUserId(), user.getEmail());
        LocalDateTime refreshExpiresAt = LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs));

        saveRefreshToken(user, refreshToken, refreshExpiresAt);
        return new AuthTokensDto(accessToken, refreshToken, refreshExpiresAt);
    }

    /** 리플래시 토큰 저장 */
    @Transactional
    public RefreshToken saveRefreshToken(User user, String token, LocalDateTime expiresAt) {
        // 기존 Refresh Token 삭제
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // 새 Refresh Token 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /** 액세스 토큰 재발급 */
    @Transactional
    public String refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리플래시 토큰"));

        // 만료 확인
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("만료된 리플래시 토큰");
        }

        // 토큰 타입 확인
        if (!jwtTokenizer.isRefreshToken(refreshTokenValue)) {
            throw new RuntimeException("리플래시 토큰 아님");
        }

        User user = refreshToken.getUser();
        return jwtTokenizer.generateAccessToken(user.getUserId(), user.getEmail());
    }

    /** 리플래시 삭제 */
    @Transactional
    public void deleteRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    /** 모든 토큰 삭제 */
    @Transactional
    public void deleteAllRefreshTokensByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    /** 만료된 토큰 정리 */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}

