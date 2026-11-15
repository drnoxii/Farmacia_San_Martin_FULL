export interface DevolucionVentaResponse {
  idVenta: number;
  importeDevuelto: number;
  lotes: { idLote: number; producto: string; numeroLote: string; stockActual: number }[];
}
