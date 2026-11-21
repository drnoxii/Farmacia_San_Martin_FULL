import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const api = request.url.startsWith('/api/') || request.url.startsWith('http://localhost:8080/api/');
  const login = request.url.split('?')[0].endsWith('/api/auth/login');
  const token = auth.getToken();
  const autenticada = api && !login && token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;
  return next(autenticada).pipe(catchError(error => {
    if (api && !login && error.status === 401) {
      const actual = auth.getToken();
      // Una petición antigua no debe cerrar una sesión que acaba de renovarse.
      if (!actual || actual === token) auth.cerrarSesion();
    }
    return throwError(() => error);
  }));
};
