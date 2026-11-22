
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MovimientoInventarioService } from
  '../../core/services/movimiento-inventario.service';

import {
  PaginaMovimientos
} from '../../core/interfaces/movimiento-inventario.interface';

@Component({
  selector: 'app-kardex',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kardex.component.html',
  styleUrl: './kardex.component.css'
})
export class KardexComponent implements OnInit {

  movimientos: PaginaMovimientos | null = null;

  paginaActual = 0;
  tamano = 20;
  cargando = false;
  error = '';

  constructor(
    private movimientoService: MovimientoInventarioService
  ) {}

  ngOnInit(): void {
    this.cargarMovimientos();
  }

  cargarMovimientos(pagina: number = 0): void {

    if (this.cargando || pagina < 0) return;

    this.cargando = true;
    this.error = '';

    this.movimientoService
      .listar(pagina, this.tamano)
      .subscribe({
        next: respuesta => {
          this.movimientos = respuesta;
          this.paginaActual = respuesta.number;
          this.cargando = false;
        },

        error: err => {
          this.cargando = false;

          if (err.status === 403) {
            this.error =
              'No tienes permisos para consultar el Kardex.';
          } else if (err.status === 401) {
            this.error =
              'Tu sesión no está autenticada. Inicia sesión nuevamente.';
          } else {
            this.error =
              'No se pudo cargar el historial de inventario.';
          }
        }
      });
  }

  siguiente(): void {
    if (
      this.movimientos &&
      !this.movimientos.last
    ) {
      this.cargarMovimientos(this.paginaActual + 1);
    }
  }

  anterior(): void {
    if (this.paginaActual > 0) {
      this.cargarMovimientos(this.paginaActual - 1);
    }
  }
}
