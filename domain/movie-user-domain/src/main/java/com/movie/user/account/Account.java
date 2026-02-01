package com.movie.user.account;

import lombok.Getter;

import java.util.Objects;

// Aggregate Root
public class Account {

    // Aggregate Root 의 정체성을 나타내는 email, accountId
    @Getter
    private final Email email;
    @Getter
    private final AccountId id;

    private Password password;      // 비밀번호 변경 가능/ 상태에 해당
    @Getter
    private AccountStatus status;
    @Getter
    private UserRole role;

    private int loginFailCount;

    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    private Account(
            AccountId id,
            Email email,
            Password password,
            AccountStatus status,
            UserRole role,
            int loginFailCount
    ) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.status = Objects.requireNonNull(status);
        this.role = Objects.requireNonNull(role);
        this.loginFailCount = loginFailCount;
    }

    /* ---------- Domain Behavior ---------- */

    public void authenticate(String rawPassword, PasswordMatcher matcher) {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }

        if (!password.matches(rawPassword, matcher)) {
            increaseFailCount();
            throw new InvalidPasswordException();
        }

        resetFailCount();
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

    private void increaseFailCount() {
        loginFailCount++;
        if (loginFailCount >= MAX_LOGIN_FAIL_COUNT) {
            this.status = AccountStatus.LOCKED;
        }
    }

    private void resetFailCount() {
        loginFailCount = 0;
    }

}