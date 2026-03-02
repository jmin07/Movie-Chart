package com.movie.core.usecase.auth.port;

import com.movie.core.usecase.auth.model.SocialUserProfile;
import com.movie.user.account.social.SocialProvider;

public interface SocialProfilePort {
    SocialUserProfile fetchProfile(SocialProvider provider, String accessToken);
}
