package com.movie.core.usecase.auth.port;

import com.movie.user.account.social.SocialProvider;
import jakarta.servlet.http.HttpServletRequest;

public interface SocialAuthorizationPort {

    SocialAuthorizationInfo buildAuthorizationInfo(SocialProvider provider, HttpServletRequest request);

    void validateState(SocialProvider provider, String receivedState, HttpServletRequest request);

    String resolveRedirectUri(SocialProvider provider);

    String successUri();

    String failureUri();

    String resolveLogoutUrl(SocialProvider provider);

    record SocialAuthorizationInfo(
            String clientId,
            String redirectUri,
            String state,
            String authorizationUrl
    ) {
    }
}
