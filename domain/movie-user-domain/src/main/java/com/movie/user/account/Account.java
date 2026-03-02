package com.movie.user.account;

import com.movie.user.account.exception.AccountLockedException;
import com.movie.user.account.exception.AccountNotActiveException;
import com.movie.user.account.exception.InvalidPasswordException;
import com.movie.user.account.social.SocialProvider;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

// Aggregate Root
public class Account {

    // Aggregate Root 의 정체성을 나타내는 email, accountId

    private @Getter final Email email;
    private @Getter final AccountId id;

    private @Getter Password password;      // 비밀번호 변경 가능/ 상태에 해당

    private @Getter AccountStatus status;
    private @Getter UserRole role;
    private @Getter SocialProvider socialProvider;
    private @Getter String providerUserId;
    private @Getter String profileImageUrl;
    private @Getter LocalDateTime lastLoginAt;

    private @Getter int loginFailCount;

    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    private Account(
            AccountId id,
            Email email,
            Password password,
            AccountStatus status,
            UserRole role,
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
        this.role = role;
        this.socialProvider = socialProvider;
        this.providerUserId = providerUserId;
        this.profileImageUrl = profileImageUrl;
        this.lastLoginAt = lastLoginAt;
        this.loginFailCount = loginFailCount;
    }

    // DB -> Domain
    public static Account restore(
            AccountId id,
            Email email,
            Password password,
            AccountStatus status,
            UserRole role,
            SocialProvider socialProvider,
            String providerUserId,
            String profileImageUrl,
            LocalDateTime lastLoginAt,
            int loginFailCount
    ) {
        return new Account(
                id,
                email,
                password,
                status,
                role,
                socialProvider,
                providerUserId,
                profileImageUrl,
                lastLoginAt,
                loginFailCount
        );
    }

    /* ---------- Domain Behavior ---------- */

    public static Account create(
            Email email,
            Password encodedpassword,
            UserRole role
    ) {
        return new Account(
            null,       // DB에 아직 저장 안 됨
            email,
            encodedpassword,
            AccountStatus.ACTIVE,
            role,
            SocialProvider.LOCAL,
            null,
            null,
            null,
            0
        );
    }

    public static Account createSocial(
            Email email,
            Password encodedPassword,
            UserRole role,
            SocialProvider socialProvider,
            String providerUserId,
            String profileImageUrl
    ) {
        return new Account(
                null,
                email,
                encodedPassword,
                AccountStatus.ACTIVE,
                role,
                socialProvider,
                providerUserId,
                profileImageUrl,
                null,
                0
        );
    }

    public boolean updateProfileImageUrlIfChanged(String newProfileImageUrl) {
        if (Objects.equals(this.profileImageUrl, newProfileImageUrl)) {
            return false;
        }
        this.profileImageUrl = newProfileImageUrl;
        return true;
    }

    public void authenticate(String rawPassword, PasswordMatcher matcher) {
        validateLoginAvailable();

        if (!password.matches(rawPassword, matcher)) {
            increaseFailCount();

            throw new InvalidPasswordException();
        }

        resetFailCount();
    }

    public void markLoginSuccess() {
        this.lastLoginAt = LocalDateTime.now();
    }

    private void validateLoginAvailable() {
        if (status == AccountStatus.LOCKED) {
            throw new AccountLockedException();
        }

        if (status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
    }

    private void increaseFailCount() {
        loginFailCount++;

        if (loginFailCount >= MAX_LOGIN_FAIL_COUNT) {
            this.status = AccountStatus.LOCKED;
        }
    }

    private void resetFailCount() {
        loginFailCount = 0;
    }

    public void changePassword(
            String currentRawPassword,
            Password newEncodedPassword,
            PasswordMatcher matcher
    ) {
        if (!password.matches(currentRawPassword, matcher)) {
            throw new InvalidPasswordException();
        }
        this.password = newEncodedPassword;
    }




}
