import { CanActivateFn } from '@angular/router';

/**
 * Route guard that checks for an active MFA session before allowing navigation.
 *
 * <p>The MFA sets a session cookie ({@code mfa-session}) with {@code http-only: false}
 * so that Angular can read it via {@code document.cookie}.  If the cookie is absent
 * the browser is redirected to the OIDC login endpoint on the MFA ({@code /auth/login})
 * via a full-page navigation so that the Spring Boot controller — not the Angular router —
 * handles the request and initiates the OAuth2 Authorization Code flow.
 *
 * <p>Apply this guard to any route that requires authentication:
 * <pre>
 * { path: 'persons', canActivate: [authGuard], loadChildren: … }
 * </pre>
 */
export const authGuard: CanActivateFn = () => {
  const hasSession = document.cookie
    .split(';')
    .some(c => c.trim().startsWith('mfa-session='));

  if (!hasSession) {
    window.location.href = '/auth/login';
    return false;
  }
  return true;
};
