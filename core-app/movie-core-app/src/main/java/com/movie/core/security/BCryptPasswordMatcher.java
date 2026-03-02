package com.movie.core.security;

import com.movie.user.account.PasswordMatcher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordMatcher implements PasswordMatcher {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordMatcher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}
