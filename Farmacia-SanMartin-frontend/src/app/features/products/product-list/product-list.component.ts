import { Component, OnInit } from '@angular/core';
import { Producto } from '../../../core/interfaces/producto.interface';
import { ProductoService } from '../../../core/services/producto.service';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {

  productos: Producto[] = [];
  busqueda: string = '';
  cargando: boolean = true;

  constructor(private productoService: ProductoService) { }
  
  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos() {

    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al conectar con el backend', err);
        this.cargando = false;
      }
    });

  }

  get productosFiltrados() {
  return this.productos.filter(p => {
    const busquedaLower = this.busqueda.toLowerCase();
    // Usamos nombreProducto que es el campo real de tu backend
    const nombre = p.nombreProducto ? p.nombreProducto.toLowerCase() : '';
    
    return nombre.includes(busquedaLower);
  });
}
}
