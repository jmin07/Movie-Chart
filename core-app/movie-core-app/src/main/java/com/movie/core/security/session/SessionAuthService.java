package com.movie.core.security.session;

import com.movie.core.security.jwt.JwtTokenService;
import com.movie.user.account.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class SessionAuthService {

    public static final String AUTH_SESSION_STATE = "AUTH_SESSION_STATE";
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    private static final Duration IDLE_TIMEOUT = Duration.ofMinutes(30);
    private static final Duration ABSOLUTE_TIMEOUT = Duration.ofHours(8);
    private static final Duration REFRESH_TIMEOUT = Duration.ofHours(8);

    private final JwtTokenService jwtTokenService;

    public SessionAuthService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public AuthSessionState establishSession(HttpServletRequest request, Account account) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval((int) IDLE_TIMEOUT.getSeconds());

        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(ACCESS_TOKEN_TTL);
        Instant refreshExpiresAt = now.plus(REFRESH_TIMEOUT);
        Instant absoluteExpiresAt = now.plus(ABSOLUTE_TIMEOUT);

        String sessionId = session.getId();
        String accessToken = jwtTokenService.createAccessToken(
                account.getId().value(),
                account.getEmail().value(),
                account.getRole().name(),
                account.getSocialProvider().name(),
                sessionId,
                now,
                accessExpiresAt
        );
        String refreshToken = jwtTokenService.createRefreshToken(
                account.getId().value(),
                sessionId,
                now,
                refreshExpiresAt
        );

        AuthSessionState state = new AuthSessionState(
                account.getId().value(),
                account.getEmail().value(),
                account.getRole(),
                account.getSocialProvider(),
                accessToken,
                accessExpiresAt,
                refreshToken,
                refreshExpiresAt,
                absoluteExpiresAt
        );
        session.setAttribute(AUTH_SESSION_STATE, state);
        return state;
    }

    public AuthSessionState refreshAccessTokenIfNeeded(HttpSession session, AuthSessionState state) {
        Instant now = Instant.now();
        if (now.isBefore(state.accessTokenExpiresAt())) {
            return state;
        }

        Instant newAccessExpiresAt = now.plus(ACCESS_TOKEN_TTL);
        String newAccessToken = jwtTokenService.createAccessToken(
                state.accountId(),
                state.email(),
                state.role().name(),
                state.provider().name(),
                session.getId(),
                now,
                newAccessExpiresAt
        );

        AuthSessionState refreshed = new AuthSessionState(
                state.accountId(),
                state.email(),
                state.role(),
                state.provider(),
                newAccessToken,
                newAccessExpiresAt,
                state.refreshToken(),
                state.refreshTokenExpiresAt(),
                state.absoluteExpiresAt()
        );
        session.setAttribute(AUTH_SESSION_STATE, refreshed);
        return refreshed;
    }
}
