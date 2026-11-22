
export interface MovimientoInventario {
  idMovimiento: number;

  // Producto
  idProducto: number;
  codigoProducto: string;
  nombreProducto: string;

  // Lote
  idLote: number;
  numeroLote: string;

  // Movimiento
  tipoMovimiento: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  cantidad: number;
  stockAnterior: number;
  stockNuevo: number;

  // Origen
  referenciaTipo: 'COMPRA' | 'VENTA' | 'DEVOLUCION' | null;
  referenciaId: number | null;

  // Responsable
  idUsuario: number;
  nombreUsuario: string;

  // Auditoría
  fecha: string;
  observacion: string | null;
}

export interface PaginaMovimientos {
  content: MovimientoInventario[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
