package com.movie.core.security.social;

public class InvalidSocialStateException extends RuntimeException {

    public InvalidSocialStateException(String message) {
        super(message);
    }
}
