export interface Proveedor {
  idProveedor?: number;
  razonSocial: string;
  ruc: string;
  telefono?: string;
  correo?: string;
  direccion?: string;
  estado?: 'ACTIVO' | 'INACTIVO';
}