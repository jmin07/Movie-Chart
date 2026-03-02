package com.movie.core.security.session;

import com.movie.user.account.social.SocialProvider;
import com.movie.user.account.UserRole;

import java.io.Serializable;
import java.time.Instant;

public record AuthSessionState(
        Long accountId,
        String email,
        UserRole role,
        SocialProvider provider,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        Instant absoluteExpiresAt
) implements Serializable {
}
