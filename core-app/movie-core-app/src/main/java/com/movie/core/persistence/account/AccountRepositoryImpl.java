package com.movie.core.persistence.account;

import com.movie.user.account.Account;
import com.movie.user.account.AccountRepository;
import com.movie.user.account.Email;
import com.movie.user.account.social.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final SpringDataAccountRepository jpaRepository;

    @Override
    public Optional<Account> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value())
                .map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findBySocialProviderAndProviderUserId(SocialProvider provider, String providerUserId) {
        return jpaRepository.findBySocialProviderAndProviderUserId(provider, providerUserId)
                .map(AccountMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsBySocialProviderAndProviderUserId(SocialProvider provider, String providerUserId) {
        return jpaRepository.existsBySocialProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public void save(Account account) {
        jpaRepository.save(AccountMapper.toEntity(account));
    }


}
