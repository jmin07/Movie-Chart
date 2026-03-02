package com.movie.core.controller.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.regex.Pattern;

public record LoginRequest (
        @Schema(description = "로그인 이메일", example = "test@example.com")
        String email,
        @Schema(description = "로그인 비밀번호", example = "pass1234")
        String password
) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public LoginRequest {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
    }
}
