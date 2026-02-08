package com.movie.user.account;

import java.util.Objects;

public record AccountId(Long value) {

    public AccountId {
        Objects.requireNonNull(value, "accountId cannot be null");
    }
}
