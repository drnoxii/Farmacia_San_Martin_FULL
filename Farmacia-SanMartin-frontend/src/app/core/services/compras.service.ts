import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Compra, EstadoCompra } from '../interfaces/compras.interface';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ComprasService {
  private apiUrl = '/api/v1/compras';

  constructor(private http: HttpClient) { }

  listarCompras(): Observable<Compra[]> {
    return this.http.get<Compra[]>(this.apiUrl);
  }

  listarCompraByid(id: number): Observable<Compra> {
    return this.http.get<Compra>(`${this.apiUrl}/${id}`);
  }

  listarComprasByProveedor(idProveedor: number): Observable<Compra[]> {
    return this.http.get<Compra[]>(`${this.apiUrl}/proveedor/${idProveedor}`);
  }

  listarComprasByNombreProveedor(nombreProveedor: string): Observable<Compra[]> {
    return this.http.get<Compra[]>(`${this.apiUrl}/proveedor/nombre/${nombreProveedor}`);
  }

  actualizarEstadoCompra(id: number, estado: EstadoCompra): Observable<Compra> {
    const params= new HttpParams().set('estadoCompra', estado);
    return this.http.patch<Compra>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  //Crear una nueva compra
  crearCompra(compra: any): Observable<Compra> {
    console.log('Formad Recibido. Create. Datos de la compra a enviar:');
    console.log(compra);
    return this.http.post<Compra>(this.apiUrl, compra);
  }
}
