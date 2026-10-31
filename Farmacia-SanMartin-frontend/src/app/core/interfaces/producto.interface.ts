export interface Producto {
  idProducto?: number;
  codigo?: string;
  nombreProducto: string;
  descripcion: string;
  idCategoria: number;
  idLaboratorio: number;
  precioCompra: number;
  precioVenta: number;
  stockMinimo: number;
  estadoGeneral: 'ACTIVO' | 'INACTIVO'; 
  nombreCategoria?: string;
  stockTotal?: number;
}