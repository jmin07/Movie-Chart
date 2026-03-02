package com.movie.core.security.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class SessionTokenHeaderFilter extends OncePerRequestFilter {

    private final SessionAuthService sessionAuthService;

    public SessionTokenHeaderFilter(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Object rawState = session.getAttribute(SessionAuthService.AUTH_SESSION_STATE);
        if (!(rawState instanceof AuthSessionState state)) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant now = Instant.now();
        if (now.isAfter(state.absoluteExpiresAt()) || now.isAfter(state.refreshTokenExpiresAt())) {
            session.invalidate();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        sessionAuthService.refreshAccessTokenIfNeeded(session, state);
        response.setHeader("Cache-Control", "no-store");
        filterChain.doFilter(request, response);
    }
}
