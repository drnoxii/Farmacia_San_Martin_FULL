import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Compra } from '../interfaces/compras.interface';
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

  //Crear una nueva compra
  crearCompra(formData: FormData): Observable<Compra> {
    console.log('Formad Recibido. Create. Datos de la compra a enviar:');
    formData.forEach((value, key) => {
      console.log(`${key}: ${value}`);
    });
    return this.http.post<Compra>(this.apiUrl, formData);
  }
}
