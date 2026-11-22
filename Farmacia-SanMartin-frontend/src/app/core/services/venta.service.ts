import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { VentaRequest } from '../interfaces/venta.interface';
import { Observable } from 'rxjs';
import { VentaResponse } from '../interfaces/venta-response.interface';
import { DevolucionVentaResponse } from '../interfaces/devolucion-venta.interface';

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

  
devolver(
  id: number,
  idDetalles: number[],
  observacion: string
): Observable<DevolucionVentaResponse> {

  return this.http.post<DevolucionVentaResponse>(
    `${this.apiUrl}/${id}/devoluciones`,
    {
      idDetalles,
      observacion
    }
  );
}



}
