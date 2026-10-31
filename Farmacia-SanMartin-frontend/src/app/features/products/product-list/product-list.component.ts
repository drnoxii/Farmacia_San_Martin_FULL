import { Component, inject, OnInit } from '@angular/core';
import { Producto } from '../../../core/interfaces/producto.interface';
import { ProductoService } from '../../../core/services/producto.service';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Lote } from '../../../core/interfaces/lote.interface';
import { LoteService } from '../../../core/services/lote.service';
import { AlertService } from '../../../core/services/alert.service';


@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {

private productoService = inject(ProductoService);
  private loteService = inject(LoteService);
  private alertService = inject(AlertService);

  productos: Producto[] = [];
  busqueda: string = '';
  cargando: boolean = true;

  // Variables para el Modal de Lotes
  lotesDelProducto: Lote[] = [];
  productoSeleccionado: Producto | null = null;

  ngOnInit() {
    this.cargarProductos();
  }

  cargarProductos() {
    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al traer productos', err);
        this.cargando = false;
      }
    });
  }

  get productosFiltrados() {
    return this.productos.filter(p => 
      (p.nombreProducto?.toLowerCase() || '').includes(this.busqueda.toLowerCase()) ||
      (p.codigo?.toLowerCase() || '').includes(this.busqueda.toLowerCase())
    );
  }

  // 1. Cambiar Estado (Solo ACTIVO / INACTIVO)
  async cambiarEstado(p: Producto) {
    const nuevoEstado: 'ACTIVO' | 'INACTIVO' = p.estadoGeneral === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    
    const confirmado = await this.alertService.confirm(
      `¿Deseas cambiar el estado de ${p.nombreProducto} a ${nuevoEstado}?`,
      'Confirmar Cambio'
    );

    if (confirmado) {
      const productoActualizado: Producto = { ...p, estadoGeneral: nuevoEstado };
      
      this.productoService.actualizar(p.idProducto!, productoActualizado).subscribe({
        next: () => {
          this.alertService.toast(`Producto ${nuevoEstado.toLowerCase()} correctamente`);
          this.cargarProductos(); // Recargamos la tabla
        },
        error: () => this.alertService.error('No se pudo cambiar el estado')
      });
    }
  }

  // 2. Abrir Modal de Lotes
  verLotes(p: Producto) {
    this.productoSeleccionado = p;
    this.lotesDelProducto = []; // Limpiamos los anteriores
    
    this.loteService.listarPorProducto(p.idProducto!).subscribe({
      next: (lotes) => this.lotesDelProducto = lotes,
      error: () => this.alertService.error('Error al cargar los lotes')
    });
  }
}
