package com.movie.core.persistence.account;

import com.movie.user.account.Account;
import com.movie.user.account.AccountId;
import com.movie.user.account.Email;
import com.movie.user.account.Password;
import com.movie.user.account.social.SocialProvider;

public final class AccountMapper {

    /*
        JPA 쪽이 도메인을 알면 안 되기 때문에,그 사이에서 변환을 담당하는 게 AccountMapper 다.
        - 👉 domain은 DB/ORM을 절대 몰라야 한다
     */


    public static Account toDomain(AccountJpaEntity entity) {
        return Account.restore(
                new AccountId(entity.getId()),
                new Email(entity.getEmail()),
                Password.ofEncoded(entity.getPassword()),
                entity.getStatus(),
                entity.getUserRole(),
                entity.getSocialProvider() == null ? SocialProvider.LOCAL : entity.getSocialProvider(),
                entity.getProviderUserId(),
                entity.getProfileImageUrl(),
                entity.getLastLoginAt(),
                entity.getLoginFailCount()
        );
    }

    public static AccountJpaEntity toEntity(Account account) {
        Long id = account.getId() == null ? null : account.getId().value();
        if (id == null) {
            return AccountJpaEntity.create(
                    account.getEmail().value(),
                    account.getPassword().encoded(),
                    account.getStatus(),
                    account.getRole(),
                    account.getSocialProvider(),
                    account.getProviderUserId(),
                    account.getProfileImageUrl(),
                    account.getLastLoginAt(),
                    account.getLoginFailCount()
            );
        }

        return AccountJpaEntity.restore(
                id,
                account.getEmail().value(),
                account.getPassword().encoded(),
                account.getStatus(),
                account.getRole(),
                account.getSocialProvider(),
                account.getProviderUserId(),
                account.getProfileImageUrl(),
                account.getLastLoginAt(),
                account.getLoginFailCount()
        );
    }
}
