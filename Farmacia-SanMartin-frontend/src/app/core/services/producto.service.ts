import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Producto } from '../interfaces/producto.interface';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {

private apiUrl = '/api/v1/producto';
  constructor(private http: HttpClient) {}

    listar(): Observable<Producto[]> {
    // Cambiamos la ruta para usar la consulta con stock
    return this.http.get<Producto[]>(`${this.apiUrl}/inventario`);
  }

  crear(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto);
  }

  buscarPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  actualizar(id: number, producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorNombre(nombre: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/nombre/${nombre}`);
  }

  buscarPorLaboratorio(idLaboratorio: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/laboratorio/${idLaboratorio}`);
  }

  buscarPorEstado(estado: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/estado/${estado}`);
  }

  buscarPorCategoria(idCategoria: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/categoria/${idCategoria}`);
  }

  

}
