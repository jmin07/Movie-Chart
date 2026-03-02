package com.movie.core.usecase.auth;

import com.movie.core.usecase.auth.model.SocialUserProfile;
import com.movie.core.usecase.auth.port.SocialAuthorizationPort;
import com.movie.core.usecase.auth.port.SocialProfilePort;
import com.movie.core.usecase.auth.port.SocialTokenPort;
import com.movie.user.account.Account;
import com.movie.user.account.AccountRepository;
import com.movie.user.account.Email;
import com.movie.user.account.Password;
import com.movie.user.account.UserRole;
import com.movie.user.account.exception.DuplicateEmailException;
import com.movie.user.account.social.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialLoginUseCase {

    private final AccountRepository accountRepository;
    private final SocialProfilePort socialProfilePort;
    private final SocialTokenPort socialTokenPort;
    private final SocialAuthorizationPort socialAuthorizationPort;
    private final PasswordEncoder passwordEncoder;

    public Account loginOrRegister(
            SocialProvider provider,
            String code,
            String state
    ) {
        String redirectUri = socialAuthorizationPort.resolveRedirectUri(provider);
        String accessToken = socialTokenPort.exchangeAccessToken(provider, code, redirectUri, state);
        SocialUserProfile profile = socialProfilePort.fetchProfile(provider, accessToken);

        Account existingSocialAccount = accountRepository
                .findBySocialProviderAndProviderUserId(provider, profile.providerUserId())
                .orElse(null);
        if (existingSocialAccount != null) {
            existingSocialAccount.markLoginSuccess();
            accountRepository.save(existingSocialAccount);
            return existingSocialAccount;
        }

        String emailValue = resolveEmail(provider, profile);
        Email email = new Email(emailValue);
        if (accountRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        Password placeholderPassword = Password.ofEncoded(passwordEncoder.encode(UUID.randomUUID().toString()));
        Account newAccount = Account.createSocial(
                email,
                placeholderPassword,
                UserRole.USER,
                provider,
                profile.providerUserId(),
                null
        );
        newAccount.markLoginSuccess();
        accountRepository.save(newAccount);

        return accountRepository
                .findBySocialProviderAndProviderUserId(provider, profile.providerUserId())
                .orElseThrow(() -> new IllegalStateException("Social account was not persisted"));
    }

    private String resolveEmail(SocialProvider provider, SocialUserProfile profile) {
        if (profile.email() != null && !profile.email().isBlank()) {
            return profile.email();
        }

        if (provider == SocialProvider.KAKAO) {
            String localPart = normalizeNickname(profile.nickname());
            if (localPart.isEmpty()) {
                localPart = "kakao_" + profile.providerUserId();
            }
            return localPart + "@move.com";
        }

        throw new IllegalArgumentException("Email not provided by social provider");
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null) {
            return "";
        }
        return nickname.trim()
                .replaceAll("\\s+", "")
                .replace("@", "");
    }
}
