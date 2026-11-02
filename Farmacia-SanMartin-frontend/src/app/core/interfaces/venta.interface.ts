export interface DetalleVentaRequest {
  idLote: number;
  cantidad: number;
}

export interface VentaRequest {
  idCliente?: number | null;
  idCaja: number;
  idUsuario: number;
  tipoComprobante: 'BOLETA' | 'FACTURA';
  metodoPago: 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TARJETA';
  detalles: DetalleVentaRequest[];
}