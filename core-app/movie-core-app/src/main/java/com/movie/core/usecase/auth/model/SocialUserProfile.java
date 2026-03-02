package com.movie.core.usecase.auth.model;

public record SocialUserProfile(
        String email,
        String providerUserId,
        String nickname
) {
}
