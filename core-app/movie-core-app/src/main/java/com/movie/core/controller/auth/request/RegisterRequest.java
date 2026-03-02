package com.movie.core.controller.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import com.movie.user.account.UserRole;

import java.util.regex.Pattern;

public record RegisterRequest(
        @Schema(description = "회원 이메일", example = "test@example.com")
        String email,
        @Schema(description = "회원 비밀번호", example = "pass1234")
        String password,
        @Schema(description = "회원 권한", example = "USER")
        UserRole role
) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public RegisterRequest {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }

        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
    }
}
