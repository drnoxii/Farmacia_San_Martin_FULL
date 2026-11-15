export interface Producto {
  idProducto?: number;
  codigo?: string;
  nombreProducto: string;
  descripcion: string;
  presentacion?: string | null;
  idCategoria: number;
  idLaboratorio: number;
  precioVenta: number;
  stockMinimo: number;
  estadoGeneral: 'ACTIVO' | 'INACTIVO'; 
  nombreCategoria?: string;
  stockTotal?: number;
}