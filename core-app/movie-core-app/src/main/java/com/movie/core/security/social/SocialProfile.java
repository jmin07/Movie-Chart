package com.movie.core.security.social;

public record SocialProfile(
        String email,
        String providerUserId,
        String nickname
) {
}
