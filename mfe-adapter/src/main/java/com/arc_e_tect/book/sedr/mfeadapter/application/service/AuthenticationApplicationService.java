package com.arc_e_tect.book.sedr.mfeadapter.application.service;

import com.arc_e_tect.book.sedr.mfeadapter.application.exception.AuthenticationException;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.Session;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound.AuthenticateUserUseCase;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.IdentityProviderPort;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.SessionStorePort;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Application service handling the authentication lifecycle:
 * OAuth2 callback, session creation, and logout.
 */
@Service
public class AuthenticationApplicationService implements AuthenticateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationApplicationService.class);

    private final IdentityProviderPort identityProvider;
    private final SessionStorePort sessionStore;
    private final MfeAdapterProperties mfeAdapterProperties;

    public AuthenticationApplicationService(
            IdentityProviderPort identityProvider,
            SessionStorePort sessionStore,
            MfeAdapterProperties mfeAdapterProperties) {
        this.identityProvider = identityProvider;
        this.sessionStore = sessionStore;
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Override
    public Session authenticate(String authorizationCode, String redirectUri) {
        log.debug("Authenticating via OAuth2 authorization code flow");

        UserToken userToken = identityProvider.exchangeCodeForToken(authorizationCode, redirectUri);

        String sessionId = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(
                mfeAdapterProperties.getSession().getTtlMinutes() * 60L);

        Session session = new Session(sessionId, userToken, expiresAt, userToken.subject());
        sessionStore.save(session);

        log.info("Session created for subject '{}'", userToken.subject());
        return session;
    }

    @Override
    public void logout(String sessionId) {
        log.debug("Logging out session '{}'", sessionId);
        sessionStore.findById(sessionId).ifPresent(session -> {
            identityProvider.revokeToken(session.userToken());
            sessionStore.deleteById(sessionId);
            log.info("Session '{}' invalidated for subject '{}'", sessionId, session.subject());
        });
    }

    @Override
    public Session getValidSession(String sessionId) {
        return sessionStore.findById(sessionId)
                .filter(s -> !s.isExpired())
                .orElseThrow(() -> new AuthenticationException(
                        "Session not found or expired: " + sessionId));
    }
}
