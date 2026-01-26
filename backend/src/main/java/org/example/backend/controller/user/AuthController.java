package org.example.backend.controller.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.User;
import org.example.backend.dto.user.LoginRequestDto;
import org.example.backend.dto.user.SignupRequestDto;
import org.example.backend.security.JwtTokenizer;
import org.example.backend.service.user.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenizer jwtTokenizer;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok().build();
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<Void> login(
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

        return ResponseEntity.ok().build();
    }

    /** 액세스토큰 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
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

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
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

