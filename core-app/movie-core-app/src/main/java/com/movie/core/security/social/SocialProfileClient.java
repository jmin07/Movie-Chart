package com.movie.core.security.social;

import com.movie.core.usecase.auth.model.SocialUserProfile;
import com.movie.core.usecase.auth.port.SocialProfilePort;
import com.movie.user.account.social.SocialProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class SocialProfileClient implements SocialProfilePort {

    private final RestClient restClient;

    public SocialProfileClient() {
        this.restClient = RestClient.create();
    }

    @Override
    public SocialUserProfile fetchProfile(SocialProvider provider, String accessToken) {
        try {
            return switch (provider) {
                case GOOGLE -> fetchGoogleProfile(accessToken);
                case KAKAO -> fetchKakaoProfile(accessToken);
                case NAVER -> fetchNaverProfile(accessToken);
                default -> throw new SocialAuthenticationException("Unsupported social provider");
            };
        } catch (RestClientException ex) {
            throw new SocialAuthenticationException("Failed to fetch social profile", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private SocialUserProfile fetchGoogleProfile(String accessToken) {
        Map<String, Object> body = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .body(Map.class);

        if (body == null) {
            throw new SocialAuthenticationException("Google profile response is empty");
        }
        return new SocialUserProfile(
                readString(body, "email"),
                readString(body, "sub"),
                readOptionalString(body, "name")
        );
    }

    @SuppressWarnings("unchecked")
    private SocialUserProfile fetchKakaoProfile(String accessToken) {
        Map<String, Object> body = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .body(Map.class);

        if (body == null) {
            throw new SocialAuthenticationException("Kakao profile response is empty");
        }
        Object kakaoAccountRaw = body.get("kakao_account");
        if (!(kakaoAccountRaw instanceof Map<?, ?> kakaoAccountMap)) {
            throw new SocialAuthenticationException("Kakao account payload is invalid");
        }

        return new SocialUserProfile(
                readOptionalString((Map<String, Object>) kakaoAccountMap, "email"),
                readString(body, "id"),
                extractKakaoNickname(body, (Map<String, Object>) kakaoAccountMap)
        );
    }

    @SuppressWarnings("unchecked")
    private SocialUserProfile fetchNaverProfile(String accessToken) {
        Map<String, Object> body = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .body(Map.class);

        if (body == null) {
            throw new SocialAuthenticationException("Naver profile response is empty");
        }
        Object responseRaw = body.get("response");
        if (!(responseRaw instanceof Map<?, ?> responseMap)) {
            throw new SocialAuthenticationException("Naver response payload is invalid");
        }

        Map<String, Object> response = (Map<String, Object>) responseMap;
        return new SocialUserProfile(
                readString(response, "email"),
                readString(response, "id"),
                readOptionalString(response, "nickname")
        );
    }

    private String readString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new SocialAuthenticationException("Missing field: " + key);
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractKakaoNickname(
            Map<String, Object> body,
            Map<String, Object> kakaoAccount
    ) {
        Object propertiesRaw = body.get("properties");
        if (propertiesRaw instanceof Map<?, ?> propertiesMap) {
            String nickname = readOptionalString((Map<String, Object>) propertiesMap, "nickname");
            if (nickname != null) {
                return nickname;
            }
        }

        Object profileRaw = kakaoAccount.get("profile");
        if (profileRaw instanceof Map<?, ?> profileMap) {
            return readOptionalString((Map<String, Object>) profileMap, "nickname");
        }
        return null;
    }

    private String readOptionalString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

}
