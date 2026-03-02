package com.movie.core.controller.advice;

import com.movie.core.security.social.InvalidSocialStateException;
import com.movie.core.security.social.SocialAuthenticationException;
import com.movie.user.account.exception.AccountLockedException;
import com.movie.user.account.exception.AccountNotActiveException;
import com.movie.user.account.exception.AccountNotFoundException;
import com.movie.user.account.exception.DuplicateEmailException;
import com.movie.user.account.exception.InvalidEmailException;
import com.movie.user.account.exception.InvalidPasswordException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> handleEnumError(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity
                .badRequest()
                .build();
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidEmailException.class})
    public ResponseEntity<Void> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Void> handleDuplicate(RuntimeException ex) {
        return ResponseEntity.status(409).build();
    }

    @ExceptionHandler({
            AccountNotFoundException.class,
            InvalidPasswordException.class,
            SocialAuthenticationException.class,
            InvalidSocialStateException.class
    })
    public ResponseEntity<Void> handleUnauthorized(RuntimeException ex) {
        return ResponseEntity.status(401).build();
    }

    @ExceptionHandler({AccountLockedException.class, AccountNotActiveException.class})
    public ResponseEntity<Void> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(403).build();
    }
}
