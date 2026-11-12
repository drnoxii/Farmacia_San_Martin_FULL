
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  ReporteCierres,
  ReporteInventario,
  ReporteVentas,
  ResumenVentasSP
} from '../interfaces/reporte.interface';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {

  private apiUrl = '/api/v1/reportes';

  constructor(private http: HttpClient) {}

  // ==========================================
  // 1. REPORTE DE VENTAS
  // ==========================================

  ventas(desde: string, hasta: string): Observable<ReporteVentas> {

    const params = new HttpParams()
      .set('desde', desde)
      .set('hasta', hasta);

    return this.http.get<ReporteVentas>(
      `${this.apiUrl}/ventas`,
      { params }
    );
  }

  // ==========================================
  // 2. REPORTE DE INVENTARIO
  // ==========================================

  inventario(dias: number): Observable<ReporteInventario> {

    return this.http.get<ReporteInventario>(
      `${this.apiUrl}/inventario`,
      {
        params: new HttpParams().set('dias', dias)
      }
    );
  }

  // ==========================================
  // 3. REPORTE DE CIERRES DE CAJA
  // ==========================================

  cierres(desde: string, hasta: string): Observable<ReporteCierres> {

    const params = new HttpParams()
      .set('desde', desde)
      .set('hasta', hasta);

    return this.http.get<ReporteCierres>(
      `${this.apiUrl}/cierres-caja`,
      { params }
    );
  }

  // ==========================================
  // 4. PROCEDIMIENTO ALMACENADO ORACLE
  // ==========================================

  resumenVentasSP(
    desde: string,
    hasta: string
  ): Observable<ResumenVentasSP> {

    const params = new HttpParams()
      .set('desde', desde)
      .set('hasta', hasta);

    return this.http.get<ResumenVentasSP>(
      `${this.apiUrl}/ventas/resumen-sp`,
      { params }
    );
  }

}
