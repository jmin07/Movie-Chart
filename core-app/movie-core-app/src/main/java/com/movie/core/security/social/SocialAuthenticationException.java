package com.movie.core.security.social;

public class SocialAuthenticationException extends RuntimeException {

    public SocialAuthenticationException(String message) {
        super(message);
    }

    public SocialAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
