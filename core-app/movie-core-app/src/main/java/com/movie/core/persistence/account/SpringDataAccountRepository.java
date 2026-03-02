package com.movie.core.persistence.account;

import com.movie.user.account.social.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, Long> {


    /*
        Spring Data JPA Repository 는 도메인 레이어가 아니라, ORM/DB 레이어의 인터페이스
        - 👉 도메인 언어(Email, Account)를 알면 안 된다.
     */

    Optional<AccountJpaEntity> findByEmail(String email);
    Optional<AccountJpaEntity> findBySocialProviderAndProviderUserId(SocialProvider socialProvider, String providerUserId);

    boolean existsByEmail(String email);
    boolean existsBySocialProviderAndProviderUserId(SocialProvider socialProvider, String providerUserId);
}
