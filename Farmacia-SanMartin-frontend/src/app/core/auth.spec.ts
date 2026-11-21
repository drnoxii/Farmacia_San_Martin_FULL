/// <reference types="jasmine" />

import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, provideRouter } from '@angular/router';
import { AuthService } from './services/auth.service';
import { CartService } from './services/cart.service';
import { authInterceptor } from './auth.interceptor';
import { authGuard } from './auth.guard';

describe('Sesión JWT y permisos del frontend', () => {
  let auth: AuthService;
  let http: HttpClient;
  let backend: HttpTestingController;
  let router: Router;
  const usuario = { idUsuario: 7, nombre: 'Cajero real', correo: 'caja@example.com', rol: 'CAJERO', estado: 'ACTIVO' };
  const token = (segundos = 3600) => `e30.${btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + segundos }))}.firma`;
  function entrar(segundos = 3600) {
    const jwt = token(segundos);
    auth.login(usuario.correo, 'clave-prueba').subscribe();
    backend.expectOne('/api/auth/login').flush({ token: jwt, usuario });
    return jwt;
  }
  beforeEach(() => {
    sessionStorage.removeItem('farmaciaSesion');
    TestBed.configureTestingModule({ providers: [
      provideRouter([]), provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(),
      { provide: CartService, useValue: { vaciarCarrito: jasmine.createSpy('vaciarCarrito') } }
    ] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });
  afterEach(() => { auth.limpiar(); backend.verify(); });

  it('usa el usuario real y envía Bearer únicamente a la API propia', () => {
    const jwt = entrar();
    expect(auth.getUsuario()?.idUsuario).toBe(7);
    http.get('/api/v1/cajas/usuario/7').subscribe();
    const peticion = backend.expectOne('/api/v1/cajas/usuario/7');
    expect(peticion.request.headers.get('Authorization')).toBe(`Bearer ${jwt}`);
    peticion.flush([]);
    http.get('https://example.com/datos').subscribe();
    const externa = backend.expectOne('https://example.com/datos');
    expect(externa.request.headers.has('Authorization')).toBeFalse();
    externa.flush({});
  });

  it('no autentica con credenciales rechazadas ni manda Bearer al login', () => {
    auth.login(' caja@example.com ', 'incorrecta').subscribe({ error: () => {} });
    const peticion = backend.expectOne('/api/auth/login');
    expect(peticion.request.body.correo).toBe('caja@example.com');
    expect(peticion.request.headers.has('Authorization')).toBeFalse();
    peticion.flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.getUsuario()).toBeNull();
  });

  it('cierra la sesión al vencer el token', fakeAsync(() => {
    entrar(2);
    tick(2100);
    expect(auth.getToken()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('mantiene sesión ante 403 y la cierra ante 401 de la API', () => {
    entrar();
    for (const status of [403, 401]) {
      http.get('/api/v1/usuario').subscribe({ error: () => {} });
      backend.expectOne('/api/v1/usuario').flush({}, { status, statusText: 'Denied' });
      expect(!!auth.getToken()).toBe(status === 403);
    }
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('bloquea rutas sin sesión y rutas administrativas para el cajero', () => {
    const ruta = { data: { admin: true } } as unknown as ActivatedRouteSnapshot;
    const verificar = () => TestBed.runInInjectionContext(() => authGuard(ruta, {} as RouterStateSnapshot));
    expect(String(verificar())).toBe('/login');
    entrar();
    expect(String(verificar())).toBe('/dashboard');
    ruta.data = {};
    expect(verificar()).toBeTrue();
  });
});
