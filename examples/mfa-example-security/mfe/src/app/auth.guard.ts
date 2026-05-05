import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

/**
 * Route guard that checks for an active MFA session before allowing navigation.
 *
 * <p>The MFA sets a session cookie ({@code mfa-session}) with {@code http-only: false}
 * so that Angular can read it via {@code document.cookie}.  If the cookie is absent
 * the user is redirected to the OIDC login endpoint on the MFA ({@code /auth/login}).
 *
 * <p>Apply this guard to any route that requires authentication:
 * <pre>
 * { path: 'persons', canActivate: [authGuard], loadChildren: … }
 * </pre>
 */
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const hasSession = document.cookie
    .split(';')
    .some(c => c.trim().startsWith('mfa-session='));

  if (!hasSession) {
    router.navigate(['/auth/login']);
    return false;
  }
  return true;
};
