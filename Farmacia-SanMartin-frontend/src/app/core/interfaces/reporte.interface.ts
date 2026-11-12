export interface ItemResumen { etiqueta: string; cantidad: number; total: number; }

export interface VentaFila {
  numeroVenta: string; fecha: string; cliente: string; vendedor: string;
  tipoComprobante: string; metodoPago: string; total: number; estado: 'EMITIDA' | 'ANULADA';
}

export interface ReporteVentas {
  desde: string; hasta: string;
  totalVendido: number; cantidadVentas: number;
  cantidadAnuladas: number; totalAnulado: number;
  porMetodoPago: ItemResumen[]; porComprobante: ItemResumen[]; porVendedor: ItemResumen[];
  ventas: VentaFila[];
}

export interface StockBajoFila {
  codigo: string; producto: string; categoria: string;
  stockActual: number; stockMinimo: number; faltante: number;
}

export interface LoteFila {
  codigo: string; producto: string; numeroLote: string;
  fechaVencimiento: string; stock: number; diasRestantes: number;
}

export interface ReporteInventario {
  fecha: string; dias: number;
  stockBajo: StockBajoFila[]; lotesPorVencer: LoteFila[]; lotesVencidos: LoteFila[];
}

export interface CierreFila {
  cajero: string; apertura: string; cierre: string;
  montoInicial: number; ventasEfectivo: number; esperado: number;
  contado: number; diferencia: number;
}

export interface ReporteCierres {
  desde: string; hasta: string; cantidad: number;
  totalSobrantes: number; totalFaltantes: number; cierres: CierreFila[];
}

export interface ResumenVentasSP {
  cantidadRegistros: number;
  totalRegistrado: number;
  origen: string;
}
