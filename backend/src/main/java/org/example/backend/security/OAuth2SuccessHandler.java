package org.example.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.service.user.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenizer jwtTokenizer;
    private final AuthService authService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        Long userId = customPrincipal.getUserId();
        String email = customPrincipal.getUser().getEmail();

        // 액세스 토큰 생성
        String accessToken = jwtTokenizer.generateAccessToken(userId, email);
        
        // 리플래시 토큰 생성
        String refreshToken = jwtTokenizer.generateRefreshToken(userId, email);
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs));

        authService.saveRefreshToken(customPrincipal.getUser(), refreshToken, expiresAt);

        // 액세스 -> HttpOnly 쿠키
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(request.isSecure());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenizer.getExpirationTime() / 1000)); // 초 단위
        response.addCookie(accessTokenCookie);

        // 리플래시 -> HttpOnly 쿠키
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(request.isSecure());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshExpirationMs / 1000)); // 초 단위
        response.addCookie(refreshTokenCookie);

        // 로그인 성공 후 리다이렉트 (임시)
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000/oauth2/redirect");
    }
}

