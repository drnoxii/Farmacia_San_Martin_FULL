import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ComprasService } from '../../../core/services/compras.service';
import { AlertService } from '../../../core/services/alert.service';
import { Compra } from '../../../core/interfaces/compras.interface';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { Proveedor } from '../../../core/interfaces/proveedor.interface';

@Component({
  selector: 'app-compras-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './compras-list.component.html',
  styleUrl: './compras-list.component.css'
})
export class ComprasListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(RouterModule);
  private route = inject(RouterModule);
  private compraService = inject(ComprasService);
  private alertService = inject(AlertService);
  private proveedorService = inject(ProveedorService);

  busqueda: string = '';
  cargando: boolean = true;
  compras: Compra[] = [];
  proveedor: Proveedor[] = [];
  compraSeleccionada: Compra | null = null;

  ngOnInit() {
    this.cargarCompras();
    this.proveedorService.listarProveedores().subscribe(data => {
      this.proveedor = data;
    });
  }

  cargarCompras() {
    this.compraService.listarCompras().subscribe({
      next: (data) => {
        this.compras = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al traer compras', err);
        this.alertService.error('Error al cargar la lista de compras');
        this.cargando = false;
      }
    });
  }

  verDetalles(compra: Compra) {
    this.compraSeleccionada = compra;
  }
}
