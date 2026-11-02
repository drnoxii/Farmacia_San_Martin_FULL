export type EstadoCompra = 'PENDIENTE' | 'RECIBIDA' | 'ANULADA';

export interface DetalleCompra {
  idDetalleCompra?: number;
  idProducto: number;
  nombreProducto?: string;
  cantidad: number;
  precioCosto: number;
  subtotal?: number;
  numeroLote: string;
  fechaVencimiento: string;
  estadoLote?: 'ACTIVO' | 'VENCIDO' | 'AGOTADO';
}

export interface Compra {
  idCompra?: number;
  idProveedor: number;
  razonSocialProveedor?: string;
  idUsuario: number;
  nombreUsuario?: string;
  fechaCompra?: string;
  totalCompra?: number;
  estado?: EstadoCompra;
  detalles?: DetalleCompra[];
  detalleCompra?: DetalleCompra[];
}
