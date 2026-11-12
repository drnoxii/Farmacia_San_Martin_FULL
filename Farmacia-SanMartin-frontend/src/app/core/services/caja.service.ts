import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Caja, CajaResumen } from '../interfaces/caja.interface';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class CajaService {
  private apiUrl = '/api/v1/cajas';

  constructor(private http: HttpClient) {}

  // Devuelve null si el usuario no tiene caja abierta (HTTP 204)
 cajaAbierta(idUsuario: number): Observable<CajaResumen | null> {
    // Llamamos al endpoint de lista
    return this.http.get<Caja[]>(`${this.apiUrl}/usuario/${idUsuario}`).pipe(
      map(cajas => {
        // Buscamos la que esté abierta
        const abierta = cajas.find(c => c.estado === 'ABIERTA');
        // Si existe, la retornamos (casteada a CajaResumen); si no, null
        return abierta ? (abierta as unknown as CajaResumen) : null;
      })
    );
  }

  resumen(idCaja: number): Observable<CajaResumen> {
    return this.http.get<CajaResumen>(`${this.apiUrl}/${idCaja}/resumen`);
  }

  historial(idUsuario: number): Observable<Caja[]> {
    return this.http.get<Caja[]>(`${this.apiUrl}/usuario/${idUsuario}`);
  }

  abrir(idUsuario: number, montoInicial: number): Observable<Caja> {
    return this.http.post<Caja>(`${this.apiUrl}/abrir`, { idUsuario, montoInicial });
  }

  cerrar(idCaja: number, idUsuario: number, montoContado: number): Observable<Caja> {
    return this.http.post<Caja>(`${this.apiUrl}/${idCaja}/cerrar`, { idUsuario, montoContado });
  }
}