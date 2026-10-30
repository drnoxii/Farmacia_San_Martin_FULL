import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Categoria } from '../interfaces/categoria.interface';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {

 private apiUrl = '/api/v1/categoria';
  constructor(private http: HttpClient) {}
  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl);
}
crear(categoria: any): Observable<Categoria> {
  return this.http.post<Categoria>(this.apiUrl, categoria);
}
}
