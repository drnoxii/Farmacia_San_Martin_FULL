export interface Lote {
  idLote: number;
  numeroLote: string;
  stockLote: number;
  fechaVencimiento: string;
  estado: 'ACTIVO' | 'VENCIDO' | 'AGOTADO';
  idProducto?: number;
}