package com.movie.core.usecase.auth;

import com.movie.user.account.*;
import com.movie.user.account.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AccountRepository accountRepository;
    private final PasswordMatcher passwordMatcher;

    /*
        원칙
         - UseCase 에서는 DTO 를 받지 않는다.
          - DTO 는 API 요청(JSON)이나 UI 상태를 위해 만들어진 구조입니다.
          - Use Case 에 DTO 를 넘기면, UI가 변경될 때마다 비즈니스 로직(UseCase)을 수정해야 할 수 있습니다.
     */

    public Account login(String email, String password) {

        // 01. 계정 찾기
        Account account = accountRepository
                .findByEmail(new Email(email))
                .orElseThrow(AccountNotFoundException::new);

        // 02. 로그인 가능한지 도메인에게 위임
        account.authenticate(password, passwordMatcher);
        account.markLoginSuccess();

        // 03. 상태 변경 반영
        accountRepository.save(account);
        return account;
    }

}
