import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Lote } from '../interfaces/lote.interface';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoteService {
   private apiUrl = '/api/lotes';

  constructor(private http: HttpClient) { }

  listarPorProducto(idProducto: number): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/producto/${idProducto}`);
  }
}
