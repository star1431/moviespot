package org.example.backend.controller.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.User;
import org.example.backend.dto.common.MessageResponseDto;
import org.example.backend.dto.user.LoginRequestDto;
import org.example.backend.dto.user.SignupRequestDto;
import org.example.backend.security.CustomPrincipal;
import org.example.backend.security.JwtTokenizer;
import org.example.backend.service.user.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenizer jwtTokenizer;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    /** 일반 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<MessageResponseDto> signup(@RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok(new MessageResponseDto("회원가입 완료"));
    }

    /** 일반 로그인 (Access/Refresh 쿠키 발급 + RefreshToken DB 저장) */
    @PostMapping("/login")
    public ResponseEntity<MessageResponseDto> login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody LoginRequestDto requestDto
    ) {
        User user = authService.login(requestDto);
        var tokens = authService.issueTokens(user);

        Cookie accessTokenCookie = new Cookie("accessToken", tokens.accessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(request.isSecure());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenizer.getExpirationTime() / 1000));
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens.refreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(request.isSecure());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshExpirationMs / 1000));
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new MessageResponseDto("로그인 완료"));
    }

    /** Refresh Token으로 Access Token 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Access Token 재발급
            String newAccessToken = authService.refreshAccessToken(refreshToken);

            // 새 Access Token을 쿠키에 저장
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(request.isSecure());
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) (jwtTokenizer.getExpirationTime() / 1000));
            response.addCookie(accessTokenCookie);

            Map<String, String> result = new HashMap<>();
            result.put("accessToken", newAccessToken);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            // DB에서 Refresh Token 삭제
            authService.deleteRefreshToken(refreshToken);
        }

        // 쿠키 삭제
        deleteCookie(response, "accessToken", request.isSecure());
        deleteCookie(response, "refreshToken", request.isSecure());

        return ResponseEntity.ok().build();
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void deleteCookie(HttpServletResponse response, String cookieName, boolean secure) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

