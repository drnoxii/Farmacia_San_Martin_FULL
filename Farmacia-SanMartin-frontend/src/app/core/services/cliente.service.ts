import { Injectable } from '@angular/core';
import { Cliente, PersonaConsultaDni } from '../interfaces/cliente.interface';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private apiUrl = '/api/v1/clientes';

  constructor(private http: HttpClient) {}

  buscarPorDni(dni: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/dni/${dni}`);
  }

  crearCliente(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, cliente);
  }


consultarDni(dni: string): Observable<PersonaConsultaDni> {
  return this.http.post<PersonaConsultaDni>(
    '/api/personas/consultar',
    { numeroDocumento: dni }
  );
}


   

}
