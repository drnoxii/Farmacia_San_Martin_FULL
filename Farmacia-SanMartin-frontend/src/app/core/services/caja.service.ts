import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Caja, CajaResumen } from '../interfaces/caja.interface';

@Injectable({ providedIn: 'root' })
export class CajaService {
  private apiUrl = '/api/v1/cajas';

  constructor(private http: HttpClient) {}

  // Devuelve null si el usuario no tiene caja abierta (HTTP 204)
  cajaAbierta(idUsuario: number): Observable<CajaResumen | null> {
    return this.http.get<CajaResumen | null>(`${this.apiUrl}/usuario/${idUsuario}/abierta`);
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