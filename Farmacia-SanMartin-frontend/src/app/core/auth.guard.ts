import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

export const authGuard: CanActivateChildFn = route => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const usuario = auth.getUsuario();
  if (!usuario) {
    auth.limpiar();
    return router.createUrlTree(['/login']);
  }
  return !route.data['admin'] || usuario.rol === 'ADMIN'
    ? true : router.createUrlTree(['/dashboard']);
};
