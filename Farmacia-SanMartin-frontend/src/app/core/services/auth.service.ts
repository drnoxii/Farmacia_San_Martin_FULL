import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  getUsuario(): { idUsuario: number; nombre: string; rol?: string } | null {
    const usuarioGuardado = localStorage.getItem('usuario');
    if (usuarioGuardado) {
      try {
        return JSON.parse(usuarioGuardado);
      } catch (e) {
        console.error('Error parseando usuario en localStorage', e);
      }
    }
    return { idUsuario: 1, nombre: 'Admin Prueba', rol: 'ADMIN' };
  }
}
