package com.movie.core.security.oauth2;

import com.movie.user.account.Account;
import com.movie.user.account.AccountRepository;
import com.movie.user.account.Email;
import com.movie.user.account.Password;
import com.movie.user.account.UserRole;
import com.movie.user.account.social.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public Account getOrRegister(OAuth2AuthenticationToken token) {
        SocialProvider provider = toSocialProvider(token.getAuthorizedClientRegistrationId());
        OAuth2User oauth2User = token.getPrincipal();
        String providerUserId = extractProviderUserId(provider, oauth2User);
        String email = resolveEmail(provider, oauth2User, providerUserId);

        Account existingAccount = accountRepository
                .findBySocialProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);
        if (existingAccount != null) {
            existingAccount.markLoginSuccess();
            accountRepository.save(existingAccount);
            return existingAccount;
        }

        Email emailVo = new Email(email);

        String placeholderRawPassword = UUID.randomUUID().toString();
        Password encodedPassword = Password.ofEncoded(passwordEncoder.encode(placeholderRawPassword));

        Account account = Account.createSocial(
                emailVo,
                encodedPassword,
                UserRole.USER,
                provider,
                providerUserId,
                null
        );
        account.markLoginSuccess();
        accountRepository.save(account);
        return accountRepository.findBySocialProviderAndProviderUserId(provider, providerUserId)
                .orElseThrow(() -> new IllegalStateException("Social account was not persisted"));
    }

    private SocialProvider toSocialProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> SocialProvider.GOOGLE;
            case "kakao" -> SocialProvider.KAKAO;
            case "naver" -> SocialProvider.NAVER;
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    @SuppressWarnings("unchecked")
    private String resolveEmail(
            SocialProvider provider,
            OAuth2User user,
            String providerUserId
    ) {
        Map<String, Object> attributes = user.getAttributes();
        Object email;

        switch (provider) {
            case GOOGLE -> email = attributes.get("email");
            case NAVER -> {
                Object response = attributes.get("response");
                if (!(response instanceof Map<?, ?> responseMap)) {
                    throw new IllegalArgumentException("Missing naver response attributes");
                }
                email = ((Map<String, Object>) responseMap).get("email");
            }
            case KAKAO -> {
                Object kakaoAccount = attributes.get("kakao_account");
                if (!(kakaoAccount instanceof Map<?, ?> kakaoAccountMap)) {
                    throw new IllegalArgumentException("Missing kakao_account attributes");
                }
                email = ((Map<String, Object>) kakaoAccountMap).get("email");
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        if (email instanceof String emailValue && !emailValue.isBlank()) {
            return emailValue;
        }

        if (provider == SocialProvider.KAKAO) {
            String nickname = extractKakaoNickname(attributes);
            String localPart = normalizeNickname(nickname);
            if (localPart.isEmpty()) {
                localPart = "kakao_" + providerUserId;
            }
            return localPart + "@move.com";
        }

        throw new IllegalArgumentException("Email not provided by social provider");
    }

    @SuppressWarnings("unchecked")
    private String extractProviderUserId(SocialProvider provider, OAuth2User user) {
        Map<String, Object> attributes = user.getAttributes();
        Object providerUserId;

        switch (provider) {
            case GOOGLE -> providerUserId = attributes.get("sub");
            case NAVER -> {
                Object response = attributes.get("response");
                if (!(response instanceof Map<?, ?> responseMap)) {
                    throw new IllegalArgumentException("Missing naver response attributes");
                }
                providerUserId = ((Map<String, Object>) responseMap).get("id");
            }
            case KAKAO -> providerUserId = attributes.get("id");
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        if (providerUserId == null || providerUserId.toString().isBlank()) {
            throw new IllegalArgumentException("Provider user id not provided by social provider");
        }
        return providerUserId.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractKakaoNickname(Map<String, Object> attributes) {
        Object propertiesRaw = attributes.get("properties");
        if (propertiesRaw instanceof Map<?, ?> propertiesMap) {
            Object nickname = ((Map<String, Object>) propertiesMap).get("nickname");
            if (nickname != null && !nickname.toString().isBlank()) {
                return nickname.toString();
            }
        }

        Object kakaoAccountRaw = attributes.get("kakao_account");
        if (kakaoAccountRaw instanceof Map<?, ?> kakaoAccountMap) {
            Object profileRaw = ((Map<String, Object>) kakaoAccountMap).get("profile");
            if (profileRaw instanceof Map<?, ?> profileMap) {
                Object nickname = ((Map<String, Object>) profileMap).get("nickname");
                if (nickname != null && !nickname.toString().isBlank()) {
                    return nickname.toString();
                }
            }
        }
        return "";
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
