
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { PaginaMovimientos } from
  '../interfaces/movimiento-inventario.interface';

@Injectable({
  providedIn: 'root'
})
export class MovimientoInventarioService {

  private apiUrl = '/api/v1/movimientos';

  constructor(private http: HttpClient) {}

  listar(
    page: number = 0,
    size: number = 20
  ): Observable<PaginaMovimientos> {

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginaMovimientos>(
      this.apiUrl,
      { params }
    );
  }
}
