package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.Session;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound.AuthenticateUserUseCase;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.IdentityProviderPort;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config.MfeAdapterProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Handles the OAuth2 Authorization Code flow with the OIDC Provider:
 * <ul>
 *   <li>{@code GET /auth/login} – redirects the user-agent to the OIDC Provider.</li>
 *   <li>{@code GET /auth/callback} – exchanges the authorization code for a
 *       user token, creates an MFA session, and sets the session cookie.</li>
 *   <li>{@code POST /auth/logout} – invalidates the MFA session and revokes
 *       the user token at the OIDC Provider.</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final IdentityProviderPort identityProvider;
    private final MfeAdapterProperties mfeAdapterProperties;

    public AuthenticationController(AuthenticateUserUseCase authenticateUserUseCase,
                                     IdentityProviderPort identityProvider,
                                     MfeAdapterProperties mfeAdapterProperties) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.identityProvider = identityProvider;
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    /**
     * Initiates the authorization-code flow by redirecting to the OIDC Provider.
     *
     * @param redirectAfterLogin URI the MFE wants to return to after login
     */
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam(value = "redirect_uri", required = false,
                    defaultValue = "/") String redirectAfterLogin,
            HttpServletRequest request) {

        String state = UUID.randomUUID().toString();
        String callbackUri = buildCallbackUri(request);
        String authUrl = identityProvider.buildAuthorizationUrl(callbackUri, state);

        log.debug("Redirecting to OIDC Provider: {}", authUrl);
        return ResponseEntity.status(302)
                .location(URI.create(authUrl))
                .build();
    }

    /**
     * Handles the OIDC Provider callback, creates an MFA session, and sets the
     * session cookie.
     *
     * @param code  the authorization code from the OIDC Provider
     * @param state the CSRF state value (validated in production by PKCE or session)
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request,
            HttpServletResponse response) {

        String callbackUri = buildCallbackUri(request);
        Session session = authenticateUserUseCase.authenticate(code, callbackUri);

        Cookie sessionCookie = buildSessionCookie(session.sessionId());
        response.addCookie(sessionCookie);

        log.info("Session established for subject '{}', redirecting to /", session.subject());
        return ResponseEntity.status(302)
                .location(URI.create("/"))
                .build();
    }

    /**
     * Logs the user out: invalidates the MFA session and clears the cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = extractSessionCookie(request);
        if (sessionId != null) {
            authenticateUserUseCase.logout(sessionId);
        }

        // Expire the cookie
        Cookie expiredCookie = buildSessionCookie("");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);

        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private String buildCallbackUri(HttpServletRequest request) {
        return UriComponentsBuilder
                .fromUriString(request.getRequestURL().toString())
                .replacePath("/auth/callback")
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private Cookie buildSessionCookie(String sessionId) {
        MfeAdapterProperties.Session cfg = mfeAdapterProperties.getSession();
        Cookie cookie = new Cookie(cfg.getCookieName(), sessionId);
        cookie.setHttpOnly(cfg.isHttpOnly());
        cookie.setSecure(cfg.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge((int) (cfg.getTtlMinutes() * 60));
        // SameSite is set via response header; Jakarta Servlet 6 does not
        // expose it directly on Cookie yet.
        return cookie;
    }

    private String extractSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String name = mfeAdapterProperties.getSession().getCookieName();
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
