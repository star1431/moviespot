package org.example.backend.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.User;
import org.example.backend.dto.user.LoginRequestDto;
import org.example.backend.dto.user.SignupRequestDto;
import org.example.backend.service.user.AuthService;
import org.example.backend.util.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

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

        cookieUtil.addAccessTokenCookie(response, tokens.accessToken());
        cookieUtil.addRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok().build();
    }

    /** 액세스토큰 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtil.getRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }

        try {
            // Access Token 재발급
            String newAccessToken = authService.refreshAccessToken(refreshToken);

            // 새 Access Token을 쿠키에 저장
            cookieUtil.addAccessTokenCookie(response, newAccessToken);

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
        String refreshToken = cookieUtil.getRefreshToken(request);

        if (refreshToken != null) {
            authService.deleteRefreshToken(refreshToken);
        }

        // 쿠키 삭제
        cookieUtil.clearAccessTokenCookie(response);
        cookieUtil.clearRefreshTokenCookie(response);

        return ResponseEntity.ok().build();
    }
}

