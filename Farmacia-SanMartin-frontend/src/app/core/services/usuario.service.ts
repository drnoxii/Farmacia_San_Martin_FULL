
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Usuario,
  RegistroUsuario,
  ActualizarUsuario,
  EstadoUsuario,
  RolUsuario
} from '../interfaces/usuario.interface';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private http = inject(HttpClient);

  private readonly apiUrl = '/api/v1/usuario';
  private readonly authUrl = '/api/auth/registro';

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  registrar(datos: RegistroUsuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.authUrl, datos);
  }

  actualizar(
    id: number,
    datos: ActualizarUsuario
  ): Observable<Usuario> {
    return this.http.put<Usuario>(
      `${this.apiUrl}/${id}`,
      datos
    );
  }

  cambiarEstado(
    id: number,
    estado: EstadoUsuario
  ): Observable<Usuario> {

    const params = new HttpParams()
      .set('estado', estado);

    return this.http.put<Usuario>(
      `${this.apiUrl}/${id}/estado`,
      null,
      { params }
    );
  }

  listarPorRol(rol: RolUsuario): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(
      `${this.apiUrl}/rol/${rol}`
    );
  }
  eliminar(id: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/${id}`);
}
}
