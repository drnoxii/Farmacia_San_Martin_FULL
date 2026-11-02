export interface DetalleVentaResponse {
  idDetalleVenta: number;
  idLote: number;
  numeroLote: string;
  fechaVencimiento: string;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  estado: string;
}

export interface VentaResponse {
  idVenta: number;
  numeroVenta: string;
  idCliente: number | null;
  nombreCliente: string | null;
  idCaja: number;
  idUsuario: number;
  nombreUsuario: string;
  tipoComprobante: string;
  metodoPago: string;
  total: number;
  estado: string;
  fechaVenta: string;
  detalles: DetalleVentaResponse[];
}