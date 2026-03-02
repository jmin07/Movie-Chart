package com.movie.user.account;

import com.movie.user.account.social.SocialProvider;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByEmail(Email email);
    Optional<Account> findBySocialProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    boolean existsByEmail(Email email);
    boolean existsBySocialProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    void save(Account account);
}
