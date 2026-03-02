package com.movie.core.usecase.auth;

import com.movie.user.account.AccountRepository;
import com.movie.user.account.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckEmailAvailabilityUseCase {

    private final AccountRepository accountRepository;

    public boolean isAvailable(String email) {
        return !accountRepository.existsByEmail(new Email(email));
    }
}
