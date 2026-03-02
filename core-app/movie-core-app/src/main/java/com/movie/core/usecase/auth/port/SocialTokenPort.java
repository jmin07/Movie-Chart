package com.movie.core.usecase.auth.port;

import com.movie.user.account.social.SocialProvider;

public interface SocialTokenPort {
    String exchangeAccessToken(
            SocialProvider provider,
            String code,
            String redirectUri,
            String state
    );
}
