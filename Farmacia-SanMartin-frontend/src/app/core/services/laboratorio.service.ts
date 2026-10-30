import { Injectable } from '@angular/core';
import { Laboratorio } from '../interfaces/laboratorio.interface';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class LaboratorioService {
  private apiUrl = '/api/v1/laboratorio';

  constructor(private http: HttpClient) { }

  listar  (): Observable<Laboratorio[]> {
    return this.http.get<Laboratorio[]>(this.apiUrl);
  }
  crear(laboratorio: any): Observable<Laboratorio> {
  return this.http.post<Laboratorio>(this.apiUrl, laboratorio);
}
}
