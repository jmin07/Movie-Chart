package com.movie.core.usecase.auth;

import com.movie.user.account.*;
import com.movie.user.account.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(
            String email,
            String rawPassword,
            UserRole role
    ) {
        Email emailVo = new Email(email);

        // 01. 이메일 중복 체크
        if (accountRepository.existsByEmail(emailVo)) {
            throw new DuplicateEmailException();
        }

        // 02. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Password password = new Password(encodedPassword);

        // 03. 계정 생성
        Account user = Account.create(emailVo, password, role);

        // 04. 계정 저장
        accountRepository.save(user);
    }

}
