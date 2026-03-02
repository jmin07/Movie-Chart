package com.movie.core.persistence.account;

import com.movie.user.account.AccountStatus;
import com.movie.user.account.UserRole;
import com.movie.user.account.social.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_social_provider_user_id", columnNames = {"social_provider", "provider_user_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider socialProvider;

    @Column(length = 128)
    private String providerUserId;

    @Column(length = 1024)
    private String profileImageUrl;

    @Column(nullable = false)
    private int loginFailCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastLoginAt;

    private AccountJpaEntity(
            Long id,
            String email,
            String password,
            AccountStatus status,
            UserRole userRole,
            SocialProvider socialProvider,
            String providerUserId,
            String profileImageUrl,
            LocalDateTime lastLoginAt,
            int loginFailCount
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.status = status;
        this.userRole = userRole;
        this.socialProvider = socialProvider;
        this.providerUserId = providerUserId;
        this.profileImageUrl = profileImageUrl;
        this.lastLoginAt = lastLoginAt;
        this.loginFailCount = loginFailCount;
    }

    public static AccountJpaEntity create(
            String email,
            String password,
            AccountStatus status,
            UserRole userRole,
            SocialProvider socialProvider,
            String providerUserId,
            String profileImageUrl,
            LocalDateTime lastLoginAt,
            int loginFailCount
    ) {
        return new AccountJpaEntity(
                null,
                email,
                password,
                status,
                userRole,
                socialProvider,
                providerUserId,
                profileImageUrl,
                lastLoginAt,
                loginFailCount
        );
    }

    public static AccountJpaEntity restore(
            Long id,
            String email,
            String password,
            AccountStatus status,
            UserRole userRole,
            SocialProvider socialProvider,
            String providerUserId,
            String profileImageUrl,
            LocalDateTime lastLoginAt,
            int loginFailCount
    ) {
        return new AccountJpaEntity(
                id,
                email,
                password,
                status,
                userRole,
                socialProvider,
                providerUserId,
                profileImageUrl,
                lastLoginAt,
                loginFailCount
        );
    }
}
