package com.movie.core.security.social;

import com.movie.core.usecase.auth.port.SocialTokenPort;
import com.movie.user.account.social.SocialProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class SocialTokenClient implements SocialTokenPort {

    private final RestClient restClient;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String kakaoClientId;
    private final String kakaoClientSecret;
    private final String naverClientId;
    private final String naverClientSecret;

    public SocialTokenClient(
            @Value("${GOOGLE_CLIENT_ID:}") String googleClientId,
            @Value("${GOOGLE_CLIENT_SECRET:}") String googleClientSecret,
            @Value("${KAKAO_CLIENT_ID:}") String kakaoClientId,
            @Value("${KAKAO_CLIENT_SECRET:}") String kakaoClientSecret,
            @Value("${NAVER_CLIENT_ID:}") String naverClientId,
            @Value("${NAVER_CLIENT_SECRET:}") String naverClientSecret
    ) {
        this.restClient = RestClient.create();
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
        this.naverClientId = naverClientId;
        this.naverClientSecret = naverClientSecret;
    }

    @Override
    public String exchangeAccessToken(
            SocialProvider provider,
            String code,
            String redirectUri,
            String state
    ) {
        try {
            return switch (provider) {
                case GOOGLE -> exchangeGoogle(code, redirectUri);
                case KAKAO -> exchangeKakao(code, redirectUri);
                case NAVER -> exchangeNaver(code, redirectUri, state);
                default -> throw new SocialAuthenticationException("Unsupported social provider");
            };
        } catch (RestClientException ex) {
            throw new SocialAuthenticationException("Failed to exchange social access token", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private String exchangeGoogle(String code, String redirectUri) {
        requireConfigured("GOOGLE_CLIENT_ID", googleClientId);
        requireConfigured("GOOGLE_CLIENT_SECRET", googleClientSecret);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", googleClientId);
        form.add("client_secret", googleClientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        Map<String, Object> body = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .body(form)
                .retrieve()
                .body(Map.class);

        return readAccessToken(body, "google");
    }

    @SuppressWarnings("unchecked")
    private String exchangeKakao(String code, String redirectUri) {
        requireConfigured("KAKAO_CLIENT_ID", kakaoClientId);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoClientId);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        if (!kakaoClientSecret.isBlank()) {
            form.add("client_secret", kakaoClientSecret);
        }

        Map<String, Object> body = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .body(form)
                .retrieve()
                .body(Map.class);

        return readAccessToken(body, "kakao");
    }

    @SuppressWarnings("unchecked")
    private String exchangeNaver(String code, String redirectUri, String state) {
        requireConfigured("NAVER_CLIENT_ID", naverClientId);
        requireConfigured("NAVER_CLIENT_SECRET", naverClientSecret);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", naverClientId);
        form.add("client_secret", naverClientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("state", state);

        Map<String, Object> body = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .body(form)
                .retrieve()
                .body(Map.class);

        return readAccessToken(body, "naver");
    }

    private String readAccessToken(Map<String, Object> body, String provider) {
        if (body == null) {
            throw new SocialAuthenticationException("Empty token response from " + provider);
        }
        Object token = body.get("access_token");
        if (token == null || token.toString().isBlank()) {
            throw new SocialAuthenticationException("Missing access_token from " + provider);
        }
        return token.toString();
    }

    private void requireConfigured(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new SocialAuthenticationException("Missing required configuration: " + key);
        }
    }
}
