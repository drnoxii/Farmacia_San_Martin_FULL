import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { CartService } from './cart.service';

export interface UsuarioSesion {
  idUsuario: number;
  nombre: string;
  correo: string;
  rol: 'ADMIN' | 'CAJERO';
  estado: string;
}
export interface LoginRespuesta { token: string; usuario: UsuarioSesion; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly clave = 'farmaciaSesion';
  private vencimiento?: ReturnType<typeof setTimeout>;

  constructor(private http: HttpClient, private router: Router, private cart: CartService) {
    this.programarVencimiento();
  }

  login(correo: string, password: string) {
    return this.http.post<LoginRespuesta>('/api/auth/login', { correo: correo.trim(), password }).pipe(tap(respuesta => {
      if (!respuesta.usuario || !['ADMIN', 'CAJERO'].includes(respuesta.usuario.rol)
          || respuesta.usuario.estado !== 'ACTIVO' || this.expira(respuesta.token) <= Date.now()) {
        throw new Error('La respuesta de inicio de sesión no es válida.');
      }
      this.limpiar();
      sessionStorage.setItem(this.clave, JSON.stringify(respuesta));
      this.programarVencimiento();
    }));
  }

  getToken(): string | null {
    const sesion = this.leer();
    return sesion && this.expira(sesion.token) > Date.now() ? sesion.token : null;
  }

  getUsuario(): UsuarioSesion | null {
    return this.getToken() ? this.leer()?.usuario ?? null : null;
  }

  cerrarSesion(): void {
    this.limpiar();
    void this.router.navigate(['/login']);
  }

  limpiar(): void {
    clearTimeout(this.vencimiento);
    if (typeof sessionStorage === 'undefined') return;
    sessionStorage.removeItem(this.clave);
    localStorage.removeItem('usuario');
    this.cart.vaciarCarrito();
  }

  private leer(): LoginRespuesta | null {
    if (typeof sessionStorage === 'undefined') return null;
    try {
      const sesion = JSON.parse(sessionStorage.getItem(this.clave) || 'null');
      return sesion && typeof sesion.token === 'string' && sesion.usuario
        && ['ADMIN', 'CAJERO'].includes(sesion.usuario.rol) ? sesion : null;
    } catch { return null; }
  }

  // Solo se lee exp para la interfaz. La firma y los permisos los valida Spring Boot.
  private expira(token: string): number {
    try {
      const partes = token.split('.');
      if (partes.length !== 3) return 0;
      const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')));
      return typeof payload.exp === 'number' && Number.isFinite(payload.exp) ? payload.exp * 1000 : 0;
    } catch { return 0; }
  }

  private programarVencimiento(): void {
    clearTimeout(this.vencimiento);
    const sesion = this.leer();
    if (!sesion) return;
    const demora = this.expira(sesion.token) - Date.now();
    if (demora <= 0) { this.limpiar(); return; }
    this.vencimiento = setTimeout(() => {
      if (this.getToken()) this.programarVencimiento();
      else this.cerrarSesion();
    }, Math.min(demora, 2147483647));
  }
}
