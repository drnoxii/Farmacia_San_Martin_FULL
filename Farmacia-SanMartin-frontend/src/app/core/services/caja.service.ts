import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Caja, CajaResumen } from '../interfaces/caja.interface';

@Injectable({ providedIn: 'root' })
export class CajaService {
  // Base de todas las rutas; el proxy la envía a http://localhost:8080
  private apiUrl = '/api/v1/cajas';

  constructor(private http: HttpClient) {}

  /**
   * QUÉ HACE: busca la caja ABIERTA del usuario y devuelve su RESUMEN completo.
   * POR QUÉ (corrección): antes se "disfrazaba" una Caja simple como CajaResumen,
   *   pero le faltaban los totales (quedaban undefined y la pantalla mostraba "S/").
   *   Ahora, al encontrar la abierta, pedimos su resumen real al backend.
   * switchMap: espera el resultado de la 1ra petición y con él lanza la 2da.
   * of(null): si no hay caja abierta, emitimos null sin hacer más peticiones.
   */
  cajaAbierta(idUsuario: number): Observable<CajaResumen | null> {
    return this.http.get<Caja[]>(`${this.apiUrl}/usuario/${idUsuario}`).pipe(
      switchMap(cajas => {
        const abierta = cajas.find(c => c.estado === 'ABIERTA');
        return abierta ? this.resumen(abierta.idCaja) : of(null);
      })
    );
  }

  /**
   * QUÉ HACE: obtiene totales por método de pago y la lista de ventas de una caja.
   * POR QUÉ: sirve tanto para la caja actual como para ver el detalle de una
   *   caja del historial (botón del ojo).
   */
  resumen(idCaja: number): Observable<CajaResumen> {
    return this.http.get<CajaResumen>(`${this.apiUrl}/${idCaja}/resumen`);
  }

  /**
   * QUÉ HACE: lista todas las cajas (abiertas y cerradas) de un usuario.
   * POR QUÉ: alimenta la tabla "Historial de cajas".
   */
  historial(idUsuario: number): Observable<Caja[]> {
    return this.http.get<Caja[]>(`${this.apiUrl}/usuario/${idUsuario}`);
  }

  /**
   * QUÉ HACE: abre una caja nueva con un monto inicial (el sencillo del cajón).
   * POR QUÉ: sin caja abierta el punto de venta no puede registrar ventas.
   */
  abrir(idUsuario: number, montoInicial: number): Observable<Caja> {
    return this.http.post<Caja>(`${this.apiUrl}/abrir`, { idUsuario, montoInicial });
  }

  /**
   * QUÉ HACE: cierra la caja enviando el efectivo que el cajero contó.
   * POR QUÉ: el backend compara ese monto con el esperado y guarda la diferencia.
   */
  cerrar(idCaja: number, idUsuario: number, montoContado: number): Observable<Caja> {
    return this.http.post<Caja>(`${this.apiUrl}/${idCaja}/cerrar`, { idUsuario, montoContado });
  }
}