import { computed, inject, Injectable, signal } from '@angular/core';
import { AlertService } from './alert.service';
import { CartItem } from '../interfaces/cart-item.interface';
import { Producto } from '../interfaces/producto.interface';
import { Lote } from '../interfaces/lote.interface';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private alertService = inject(AlertService);
  private cartItems = signal<CartItem[]>(this.loadCart());

  totalItems = computed(() => this.cartItems().reduce((acc, item) => acc + item.cantidad, 0));
  totalPrice = computed(() => this.cartItems().reduce((acc, item) => acc + (item.producto.precioVenta * item.cantidad), 0));


  getItems() {
    return this.cartItems();
  }

  private loadCart(): CartItem[] {
    const cart = localStorage.getItem('cart_farmacia');
    return cart ? JSON.parse(cart) : [];
  }

  private saveCart() {
    localStorage.setItem('cart_farmacia', JSON.stringify(this.cartItems()));
  }

  agregarProducto(producto: Producto, lote: Lote, cantidad: number) {
    const actualItem = this.cartItems();
    // Verificamos si el LOTE exacto ya está en el carrito
    const itemExistente = actualItem.find(item => item.lote.idLote === lote.idLote);

    if (itemExistente) {
      itemExistente.cantidad += cantidad;
      this.cartItems.set([...actualItem]);
    } else {
      this.cartItems.set([...actualItem, { producto, lote, cantidad }]);
    }
    this.saveCart();
    this.alertService.toast(`Añadido: ${producto.nombreProducto} (Lote ${lote.numeroLote})`);
  }

  eliminarItem(idLote: number) {
    this.cartItems.set(this.cartItems().filter(item => item.lote.idLote !== idLote));
    this.saveCart();
    this.alertService.toast('Producto eliminado');
  }

  actualizarCantidad(idLote: number, delta: number) {
    const items = this.cartItems().map(item => {
      if (item.lote.idLote === idLote) {
        const nuevaCant = item.cantidad + delta;
        // Validar que no supere el stock del lote
        return { ...item, cantidad: nuevaCant > 0 && nuevaCant <= item.lote.stockLote ? nuevaCant : item.cantidad };
      }
      return item;
    });
    this.cartItems.set(items);
    this.saveCart();
  }

  vaciarCarrito() {
    this.cartItems.set([]);
    this.saveCart();
  }
}
