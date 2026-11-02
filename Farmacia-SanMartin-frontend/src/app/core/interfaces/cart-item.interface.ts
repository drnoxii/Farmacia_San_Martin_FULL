import { Lote } from "./lote.interface";
import { Producto } from "./producto.interface";

export interface CartItem {
  producto: Producto;
  lote: Lote;
  cantidad: number;
}