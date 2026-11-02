
export type RolUsuario = 'ADMIN' | 'CAJERO';

export type EstadoUsuario = 'ACTIVO' | 'INACTIVO';

export interface Usuario {
  idUsuario: number;
  idPersona: number;
  numeroDocumento: string;
  nombre: string;
  telefono: string | null;
  direccion: string | null;
  correo: string;
  rol: RolUsuario;
  estado: EstadoUsuario;
}

export interface RegistroUsuario {
  numeroDocumento: string;
  nombre: string;
  telefono: string;
  direccion: string;
  correo: string;
  password: string;
  rol: RolUsuario;
}

export interface ActualizarUsuario {
  numeroDocumento: string;
  nombre: string;
  telefono: string;
  direccion: string;
  correo: string;
  rol: RolUsuario;
}
