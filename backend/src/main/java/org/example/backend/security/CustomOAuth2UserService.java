package org.example.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.user.User;
import org.example.backend.repository.user.UserRepository;
import org.example.backend.service.user.UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String providerId = getProviderId(oAuth2User, registrationId);
        String email = getEmail(oAuth2User, registrationId);

        if (providerId == null) {
            throw new OAuth2AuthenticationException("providerId를 가져올 수 없습니다.");
        }
        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다.");
        }

        User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
                .orElseGet(() -> {
                    // 소셜 최초 가입: 닉네임은 사용자가 직접 설정하도록 임시닉네임 적용
                    String tempNickname = generateTempNickname();

                    User newUser = User.builder()
                            .email(email)
                            .nickname(tempNickname)
                            .provider(registrationId)
                            .providerId(providerId)
                            .build();
                    return userService.save(newUser);
                });

        return new CustomPrincipal(user, oAuth2User.getAttributes());
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateTempNickname() {
        for (int i = 0; i < 10; i++) {
            String hex = Integer.toHexString(RANDOM.nextInt()).replace("-", "");
            if (hex.length() > 6) {
                hex = hex.substring(0, 6);
            }
            String candidate = "ms_" + hex;
            if (userRepository.findByNickname(candidate).isEmpty()) {
                return candidate;
            }
        }
        // 10번 시도해도 중복된거 있으면 걍 시간처리
        return "ms_" + System.currentTimeMillis();
    }

    /** oauth 제공자별 providerId 추출 */
    private String getProviderId(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                yield response != null ? (String) response.get("id") : null;
            }
            case "kakao" -> String.valueOf(oAuth2User.getAttribute("id"));
            default -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자입니다: " + registrationId);
        };
    }

    /** oauth 제공자별 이메일 추출 */
    private String getEmail(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("email");
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                yield response != null ? (String) response.get("email") : null;
            }
            case "kakao" -> {
                Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
                yield kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자입니다: " + registrationId);
        };
    }
}

