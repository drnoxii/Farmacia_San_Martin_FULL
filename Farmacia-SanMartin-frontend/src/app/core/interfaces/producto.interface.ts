export interface Producto {
  idProducto?: number;
  codigo: string;
  nombreProducto: string;
  idCategoria: number;
  idLaboratorio: number;
  precioVenta: number;
  stockMinimo: number;
  estadoGeneral: 'ACTIVO' | 'INACTIVO';
  
  // Opcionales para cuando listas desde el backend
  nombreCategoria?: string; 
  nombreLaboratorio?: string;
  stockActual?: number; 
}