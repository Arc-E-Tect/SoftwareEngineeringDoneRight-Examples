package com.arc_e_tect.examples.mfeadapter.application.service;

import com.arc_e_tect.examples.mfeadapter.application.exception.AuthenticationException;
import com.arc_e_tect.examples.mfeadapter.domain.model.Session;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.IdentityProviderPort;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.SessionStorePort;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationApplicationService")
class AuthenticationApplicationServiceTest {

    @Mock
    private IdentityProviderPort identityProvider;

    @Mock
    private SessionStorePort sessionStore;

    private MfeAdapterProperties props;
    private AuthenticationApplicationService service;

    @BeforeEach
    void setUp() {
        props = new MfeAdapterProperties();
        props.getSession().setTtlMinutes(30);
        props.getSession().setCookieName("mfa-session");

        service = new AuthenticationApplicationService(identityProvider, sessionStore, props);
    }

    // -----------------------------------------------------------------
    // authenticate
    // -----------------------------------------------------------------

    @Test
    @DisplayName("authenticate – exchanges code, creates session, saves it, and returns it")
    void authenticate_validCode_returnsNewSession() {
        UserToken userToken = new UserToken("tok-abc", Instant.now().plusSeconds(300), "user-123");
        when(identityProvider.exchangeCodeForToken(anyString(), anyString())).thenReturn(userToken);

        Session session = service.authenticate("auth-code", "http://localhost/auth/callback");

        assertThat(session).isNotNull();
        assertThat(session.subject()).isEqualTo("user-123");
        assertThat(session.userToken()).isEqualTo(userToken);
        verify(sessionStore).save(session);
    }

    @Test
    @DisplayName("authenticate – session ID is a valid UUID")
    void authenticate_sessionId_isUuid() {
        UserToken userToken = new UserToken("tok", Instant.now().plusSeconds(300), "sub");
        when(identityProvider.exchangeCodeForToken(anyString(), anyString())).thenReturn(userToken);

        Session session = service.authenticate("code", "http://localhost/callback");

        assertThat(session.sessionId()).matches("[0-9a-f-]{36}");
    }

    // -----------------------------------------------------------------
    // logout
    // -----------------------------------------------------------------

    @Test
    @DisplayName("logout – session found: revokes token and deletes session")
    void logout_sessionFound_revokesAndDeletes() {
        UserToken userToken = new UserToken("tok", Instant.now().plusSeconds(300), "sub");
        Session session = new Session("sess-id", userToken, Instant.now().plusSeconds(1800), "sub");
        when(sessionStore.findById("sess-id")).thenReturn(Optional.of(session));

        service.logout("sess-id");

        verify(identityProvider).revokeToken(userToken);
        verify(sessionStore).deleteById("sess-id");
    }

    @Test
    @DisplayName("logout – session not found: no-op, no revocation attempted")
    void logout_sessionNotFound_noOp() {
        when(sessionStore.findById("missing")).thenReturn(Optional.empty());

        service.logout("missing");

        verify(identityProvider, never()).revokeToken(any());
        verify(sessionStore, never()).deleteById(anyString());
    }

    // -----------------------------------------------------------------
    // getValidSession
    // -----------------------------------------------------------------

    @Test
    @DisplayName("getValidSession – non-expired session found: returns it")
    void getValidSession_nonExpiredSession_returnsIt() {
        UserToken userToken = new UserToken("tok", Instant.now().plusSeconds(300), "sub");
        Session session = new Session("sess-id", userToken, Instant.now().plusSeconds(1800), "sub");
        when(sessionStore.findById("sess-id")).thenReturn(Optional.of(session));

        Session result = service.getValidSession("sess-id");

        assertThat(result).isSameAs(session);
    }

    @Test
    @DisplayName("getValidSession – session expired: throws AuthenticationException")
    void getValidSession_expiredSession_throwsAuthenticationException() {
        UserToken userToken = new UserToken("tok", Instant.now().minusSeconds(1), "sub");
        Session expiredSession = new Session("sess-id", userToken, Instant.now().minusSeconds(1), "sub");
        when(sessionStore.findById("sess-id")).thenReturn(Optional.of(expiredSession));

        assertThatThrownBy(() -> service.getValidSession("sess-id"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("sess-id");
    }

    @Test
    @DisplayName("getValidSession – session not found: throws AuthenticationException")
    void getValidSession_sessionNotFound_throwsAuthenticationException() {
        when(sessionStore.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getValidSession("missing"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("missing");
    }
}
