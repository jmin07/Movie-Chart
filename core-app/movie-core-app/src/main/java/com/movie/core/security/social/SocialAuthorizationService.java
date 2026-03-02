package com.movie.core.security.social;

import com.movie.core.usecase.auth.port.SocialAuthorizationPort;
import com.movie.user.account.social.SocialProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
public class SocialAuthorizationService implements SocialAuthorizationPort {

    private static final String SOCIAL_STATE_PREFIX = "SOCIAL_AUTH_STATE_";

    private final String googleClientId;
    private final String kakaoClientId;
    private final String naverClientId;
    private final String googleRedirectUri;
    private final String kakaoRedirectUri;
    private final String naverRedirectUri;
    private final String successUri;
    private final String failureUri;

    public SocialAuthorizationService(
            @Value("${GOOGLE_CLIENT_ID:}") String googleClientId,
            @Value("${KAKAO_CLIENT_ID:}") String kakaoClientId,
            @Value("${NAVER_CLIENT_ID:}") String naverClientId,
            @Value("${auth.social.redirect.google}") String googleRedirectUri,
            @Value("${auth.social.redirect.kakao}") String kakaoRedirectUri,
            @Value("${auth.social.redirect.naver}") String naverRedirectUri,
            @Value("${auth.social.frontend.success-uri}") String successUri,
            @Value("${auth.social.frontend.failure-uri}") String failureUri
    ) {
        this.googleClientId = googleClientId;
        this.kakaoClientId = kakaoClientId;
        this.naverClientId = naverClientId;
        this.googleRedirectUri = googleRedirectUri;
        this.kakaoRedirectUri = kakaoRedirectUri;
        this.naverRedirectUri = naverRedirectUri;
        this.successUri = successUri;
        this.failureUri = failureUri;
    }

    @Override
    public SocialAuthorizationInfo buildAuthorizationInfo(
            SocialProvider provider,
            HttpServletRequest request
    ) {
        String clientId = getClientId(provider);
        if (clientId.isBlank()) {
            throw new SocialAuthenticationException("Missing client id for provider: " + provider);
        }
        String redirectUri = resolveRedirectUri(provider);
        if (redirectUri.isBlank()) {
            throw new SocialAuthenticationException("Missing redirect uri for provider: " + provider);
        }

        String state = UUID.randomUUID().toString();
        HttpSession session = request.getSession(true);
        session.setAttribute(stateKey(provider), state);

        String authorizeUrl = buildAuthorizeUrl(provider, clientId, redirectUri, state);
        return new SocialAuthorizationInfo(clientId, redirectUri, state, authorizeUrl);
    }

    @Override
    public void validateState(
            SocialProvider provider,
            String receivedState,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new InvalidSocialStateException("Missing session for social state validation");
        }

        Object expected = session.getAttribute(stateKey(provider));
        session.removeAttribute(stateKey(provider));
        if (!(expected instanceof String expectedState) || expectedState.isBlank()) {
            throw new InvalidSocialStateException("Missing expected social state");
        }
        if (receivedState == null || receivedState.isBlank() || !expectedState.equals(receivedState)) {
            throw new InvalidSocialStateException("Invalid social state");
        }
    }

    @Override
    public String resolveRedirectUri(SocialProvider provider) {
        return switch (provider) {
            case GOOGLE -> googleRedirectUri;
            case KAKAO -> kakaoRedirectUri;
            case NAVER -> naverRedirectUri;
            default -> "";
        };
    }

    @Override
    public String successUri() {
        return successUri;
    }

    @Override
    public String failureUri() {
        return failureUri;
    }

    @Override
    public String resolveLogoutUrl(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> buildKakaoLogoutUrl();
            case GOOGLE, NAVER, LOCAL -> successUri;
        };
    }

    private String buildAuthorizeUrl(
            SocialProvider provider,
            String clientId,
            String redirectUri,
            String state
    ) {
        return switch (provider) {
            case GOOGLE -> UriComponentsBuilder
                    .fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                    .queryParam("response_type", "code")
                    .queryParam("client_id", clientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("scope", "email profile")
                    .queryParam("state", state)
                    .build(true)
                    .toUriString();
            case KAKAO -> UriComponentsBuilder
                    .fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
                    .queryParam("response_type", "code")
                    .queryParam("client_id", clientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("state", state)
                    .build(true)
                    .toUriString();
            case NAVER -> UriComponentsBuilder
                    .fromHttpUrl("https://nid.naver.com/oauth2.0/authorize")
                    .queryParam("response_type", "code")
                    .queryParam("client_id", clientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("state", state)
                    .build(true)
                    .toUriString();
            default -> throw new SocialAuthenticationException("Unsupported social provider");
        };
    }

    private String getClientId(SocialProvider provider) {
        return switch (provider) {
            case GOOGLE -> googleClientId;
            case KAKAO -> kakaoClientId;
            case NAVER -> naverClientId;
            default -> "";
        };
    }

    private String buildKakaoLogoutUrl() {
        if (kakaoClientId == null || kakaoClientId.isBlank()) {
            throw new SocialAuthenticationException("Missing client id for provider: KAKAO");
        }
        return UriComponentsBuilder
                .fromHttpUrl("https://kauth.kakao.com/oauth/logout")
                .queryParam("client_id", kakaoClientId)
                .queryParam("logout_redirect_uri", successUri)
                .build(true)
                .toUriString();
    }

    private String stateKey(SocialProvider provider) {
        return SOCIAL_STATE_PREFIX + provider.name();
    }
}
