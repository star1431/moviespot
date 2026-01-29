package org.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.service.user.AuthService;
import org.example.backend.util.CookieUtil;
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
    private final CookieUtil cookieUtil;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

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

        // 쿠키 설정
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // CORS_ALLOWED_ORIGINS에서 첫 번째 origin을 프론트엔드 URL로 사용
        String frontendUrl = getAllowedOrigins().split(",")[0].trim();
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth2/redirect");
    }

    private String getAllowedOrigins() {
        return allowedOrigins != null && !allowedOrigins.isEmpty() ? allowedOrigins : "http://localhost:3000";
    }
}
