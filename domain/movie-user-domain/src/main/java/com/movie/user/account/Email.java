package com.movie.user.account;

import com.movie.user.account.exception.InvalidEmailException;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email (String value) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public Email {
        Objects.requireNonNull(value);

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException();
        }
    }
}

