import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

/**
 * HTTP interceptor that adds API-key validation headers to every outbound request
 * and redirects to the login page on 401 Unauthorized responses.
 *
 * <p>In a real deployment the API Gateway validates the API key and forwards
 * these headers.  In this example the MFE adds them directly to simulate
 * the gateway's behaviour and demonstrate how the MFA reads and validates them.
 */
export const apiKeyInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const router = inject(Router);

  const modifiedReq = req.clone({
    setHeaders: {
      'X-API-Key-Validated': 'true',
      'X-API-Key-Scope': 'READ_WRITE'
    }
  });

  return next(modifiedReq).pipe(
    catchError(error => {
      if (error.status === 401) {
        router.navigate(['/auth/login']);
      }
      return throwError(() => error);
    })
  );
};
