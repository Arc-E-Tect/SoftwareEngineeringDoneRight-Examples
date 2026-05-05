package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web;

import com.arc_e_tect.examples.mfeadapter.domain.model.Session;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.port.inbound.AuthenticateUserUseCase;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.IdentityProviderPort;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController")
class AuthenticationControllerTest {

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Mock
    private IdentityProviderPort identityProvider;

    private MfeAdapterProperties props;
    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        props = new MfeAdapterProperties();
        props.getSession().setCookieName("mfa-session");
        props.getSession().setHttpOnly(false);
        props.getSession().setSecure(false);
        props.getSession().setTtlMinutes(30);

        controller = new AuthenticationController(authenticateUserUseCase, identityProvider, props);
    }

    private MockHttpServletRequest buildRequest(String scheme, String serverName, int port, String path) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setScheme(scheme);
        req.setServerName(serverName);
        req.setServerPort(port);
        req.setRequestURI(path);
        return req;
    }

    // -----------------------------------------------------------------
    // GET /auth/login
    // -----------------------------------------------------------------

    @Test
    @DisplayName("login – redirects to OIDC provider authorization URL")
    void login_redirectsToOidcProvider() {
        when(identityProvider.buildAuthorizationUrl(anyString(), anyString()))
                .thenReturn("http://idp/auth?response_type=code&state=abc");

        MockHttpServletRequest req = buildRequest("http", "localhost", 8080, "/auth/login");
        ResponseEntity<Void> response = controller.login("/", req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).hasToString("http://idp/auth?response_type=code&state=abc");
    }

    @Test
    @DisplayName("login – passes callback URI built from request to identity provider")
    void login_passesCallbackUriToIdp() {
        when(identityProvider.buildAuthorizationUrl(anyString(), anyString()))
                .thenReturn("http://idp/auth");

        MockHttpServletRequest req = buildRequest("https", "myapp.example.com", 443, "/auth/login");
        controller.login("/dashboard", req);

        verify(identityProvider).buildAuthorizationUrl(
                org.mockito.ArgumentMatchers.contains("/auth/callback"),
                anyString()
        );
    }

    // -----------------------------------------------------------------
    // GET /auth/callback
    // -----------------------------------------------------------------

    @Test
    @DisplayName("callback – exchanges code, sets session cookie, redirects to /")
    void callback_validCode_setsCookieAndRedirects() {
        UserToken userToken = new UserToken("tok", Instant.now().plusSeconds(300), "user-123");
        Session session = new Session("sess-abc", userToken, Instant.now().plusSeconds(1800), "user-123");
        when(authenticateUserUseCase.authenticate(anyString(), anyString())).thenReturn(session);

        MockHttpServletRequest req = buildRequest("http", "localhost", 8080, "/auth/callback");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        ResponseEntity<Void> response = controller.callback("auth-code", "state123", req, resp);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).hasToString("/");
        Cookie cookie = resp.getCookie("mfa-session");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo("sess-abc");
    }

    @Test
    @DisplayName("callback – without state param → still completes successfully")
    void callback_withoutState_stillWorks() {
        UserToken userToken = new UserToken("tok", Instant.now().plusSeconds(300), "sub");
        Session session = new Session("sess-xyz", userToken, Instant.now().plusSeconds(1800), "sub");
        when(authenticateUserUseCase.authenticate(anyString(), anyString())).thenReturn(session);

        MockHttpServletRequest req = buildRequest("http", "localhost", 8080, "/auth/callback");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        ResponseEntity<Void> response = controller.callback("auth-code", null, req, resp);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(resp.getCookie("mfa-session")).isNotNull();
    }

    // -----------------------------------------------------------------
    // POST /auth/logout
    // -----------------------------------------------------------------

    @Test
    @DisplayName("logout – with matching session cookie: delegates to use case and expires cookie")
    void logout_withMatchingCookie_logsOutAndExpiresCookie() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("mfa-session", "sess-abc"));
        MockHttpServletResponse resp = new MockHttpServletResponse();

        ResponseEntity<Void> response = controller.logout(req, resp);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authenticateUserUseCase).logout("sess-abc");
        Cookie expiredCookie = resp.getCookie("mfa-session");
        assertThat(expiredCookie).isNotNull();
        assertThat(expiredCookie.getMaxAge()).isZero();
    }

    @Test
    @DisplayName("logout – request has no cookies (null): no logout call, still expires cookie")
    void logout_noCookies_noLogoutAndExpiresCookie() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        // no cookies set → getCookies() returns null
        MockHttpServletResponse resp = new MockHttpServletResponse();

        ResponseEntity<Void> response = controller.logout(req, resp);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authenticateUserUseCase, never()).logout(anyString());
        assertThat(resp.getCookie("mfa-session")).isNotNull();
        assertThat(resp.getCookie("mfa-session").getMaxAge()).isZero();
    }

    @Test
    @DisplayName("logout – cookies present but none match session cookie name: no logout call")
    void logout_cookiesWithNoMatch_noLogoutCall() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("other-cookie", "value"));
        MockHttpServletResponse resp = new MockHttpServletResponse();

        ResponseEntity<Void> response = controller.logout(req, resp);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authenticateUserUseCase, never()).logout(anyString());
    }
}
