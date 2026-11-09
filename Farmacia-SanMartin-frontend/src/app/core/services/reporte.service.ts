import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReporteCierres, ReporteInventario, ReporteVentas } from '../interfaces/reporte.interface';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private apiUrl = '/api/v1/reportes';

  constructor(private http: HttpClient) {}

  ventas(desde: string, hasta: string): Observable<ReporteVentas> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<ReporteVentas>(`${this.apiUrl}/ventas`, { params });
  }

  inventario(dias: number): Observable<ReporteInventario> {
    return this.http.get<ReporteInventario>(`${this.apiUrl}/inventario`, {
      params: new HttpParams().set('dias', dias)
    });
  }

  cierres(desde: string, hasta: string): Observable<ReporteCierres> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<ReporteCierres>(`${this.apiUrl}/cierres-caja`, { params });
  }
}