import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { VentaRequest } from '../interfaces/venta.interface';
import { Observable } from 'rxjs';
import { VentaResponse } from '../interfaces/venta-response.interface';

@Injectable({
  providedIn: 'root'
})
export class VentaService {

  private apiUrl = '/api/v1/ventas';

  constructor(private http: HttpClient) { }

  registrarVenta(venta: VentaRequest): Observable<VentaResponse> {
    return this.http.post<VentaResponse>(this.apiUrl, venta);
  }

  buscarVenta(id: number): Observable<VentaResponse> {
    return this.http.get<VentaResponse>(`${this.apiUrl}/${id}`);
  }


}
