export interface Caja {
  idCaja: number;
  idUsuario: number;
  nombreUsuario: string;
  montoInicial: number;
  montoFinal: number | null;
  diferencia: number | null;
  estado: 'ABIERTA' | 'CERRADA';
  fechaApertura: string;
  fechaCierre: string | null;
}

export interface CajaVentaDetalle {
  producto: string;
  numeroLote: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  estado: 'V' | 'D';
}

export interface CajaVenta {
  numeroVenta: string;
  fechaVenta: string;
  cliente: string;
  vendedor: string;
  tipoComprobante: string;
  metodoPago: 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TARJETA';
  total: number;
  estado: 'EMITIDA' | 'ANULADA';
  detalles: CajaVentaDetalle[];
}

export interface CajaResumen {
  idCaja: number; // uso interno, no se muestra
  nombreUsuario: string;
  estado: 'ABIERTA' | 'CERRADA';
  fechaApertura: string;
  fechaCierre: string | null;
  montoInicial: number;
  ventasEfectivo: number;
  ventasYape: number;
  ventasPlin: number;
  ventasTarjeta: number;
  totalVendido: number;
  cantidadVentas: number;
  montoEsperadoEfectivo: number;
  montoFinal: number | null;
  diferencia: number | null;
  ventas: CajaVenta[];
}