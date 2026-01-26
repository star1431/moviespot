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
        String rawNickname = getNickname(oAuth2User, registrationId);

        if (providerId == null) {
            throw new OAuth2AuthenticationException("providerId를 가져올 수 없습니다.");
        }
        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다.");
        }
        
        final String nickname = (rawNickname == null || rawNickname.isBlank()) 
                ? email.split("@")[0] 
                : rawNickname;

        User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .provider(registrationId)
                            .providerId(providerId)
                            .build();
                    return userService.save(newUser);
                });

        return new CustomPrincipal(user, oAuth2User.getAttributes());
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

    /** oauth 제공자별 닉네임 추출 */
    private String getNickname(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("name");
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                yield response != null ? (String) response.get("nickname") : null;
            }
            case "kakao" -> {
                Map<String, Object> properties = oAuth2User.getAttribute("properties");
                yield properties != null ? (String) properties.get("nickname") : null;
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자입니다: " + registrationId);
        };
    }
}

