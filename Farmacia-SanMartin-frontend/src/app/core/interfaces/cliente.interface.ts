export interface Cliente {
  idCliente?: number;
  numeroDocumento: string;
  nombre: string;
  telefono?: string;
  direccion?: string;
  sexo?: 'MASCULINO' | 'FEMENINO' | null;
}


export interface PersonaConsultaDni {
  numeroDocumento: string;
  nombre: string;
  apellidoPaterno: string | null;
  apellidoMaterno: string | null;
  direccion: string | null;
  telefono: string | null;
}
