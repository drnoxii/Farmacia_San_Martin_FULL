import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ComprasService } from '../../../core/services/compras.service';
import { AlertService } from '../../../core/services/alert.service';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { ProductoService } from '../../../core/services/producto.service';
import { AuthService } from '../../../core/services/auth.service';
import { Compra, EstadoCompra, ItemCompra } from '../../../core/interfaces/compras.interface';
import { Proveedor } from '../../../core/interfaces/proveedor.interface';
import { Producto } from '../../../core/interfaces/producto.interface';

declare const bootstrap: any;

@Component({
  selector: 'app-compras-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ReactiveFormsModule],
  templateUrl: './compras-list.component.html',
  styleUrl: './compras-list.component.css'
})
export class ComprasListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productoService = inject(ProductoService);   // ← corregido: era ComprasService por error
  private proveedorService = inject(ProveedorService);
  private compraService = inject(ComprasService);
  private authService = inject(AuthService);
  private alertService = inject(AlertService);

  busqueda = ''
  cargando: boolean = true;
  productoSeleccionado: Producto | null = null;


  compras: Compra[] = [];
  proveedor: Proveedor[] = [];
  productos: Producto[] = [];
  comprasFiltradas: Compra[] = [];

  nuevoEstadoSeleccionado: EstadoCompra | null = null;
  compraSeleccionada: Compra | null = null;
  numeroCompraSeleccionada: number | null = null;
  id : number | null = null;
  formCompra: FormGroup;
  formProveedor: FormGroup;

  terminoBusqueda: string = '';
  tipoBusqueda: 'proveedor' | 'producto' = 'proveedor';
  productoTextoBusqueda:  string = '';

  // Datos del ítem en edición
  lote = '';
  fechaVencimiento = '';
  cantidad = 1;
  precioCompra = 0;

  // Compra
  proveedores: Proveedor[] = [];
  proveedorSeleccionado: Proveedor | null = null;
  items: ItemCompra[] = [];
  guardando = false;
  nombreUsuario = '';

  constructor() {
    this.formProveedor = this.fb.group({
    razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
    telefono: ['', Validators.maxLength(15)],
    ruc: ['', [Validators.required, Validators.pattern('[0-9]{11}')]],
    correo: ['', [Validators.email, Validators.maxLength(100)]],
    direccion: ['', Validators.maxLength(250)],
    estado: ['ACTIVO']
  });
    this.formCompra = this.fb.group({
    productos: ['', [Validators.required, Validators.minLength(1)]],
    proveedor: ['', Validators.required],
    usuario: ['', [Validators.required, Validators.minLength(1)]],
    cantidad: [0, [Validators.required, Validators.min(1)]],
    precio: [0, [Validators.required, Validators.min(0.01)]],
    lote: ['', [Validators.required, Validators.minLength(3)]],
    total: [{ value: 0, disabled: true }],
    vencimiento: ['', Validators.required]
});
  }

  ngOnInit(): void {
    this.cargarCompras();

    this.proveedorService.listarProveedores().subscribe({
      next: (data) => this.proveedores = data,
      error: (err) => console.error('Error al cargar proveedores:', err)
    });

    // Cargar productos para el buscador
    this.productoService.listar().subscribe({
      next: (data) => this.productos = data,
      error: (err) => console.error('Error al cargar productos:', err)
    });

    // El usuario sale de la sesión, ya no se escribe a mano
    this.nombreUsuario = this.authService.getUsuario()?.nombre ?? '';
  }

  cargarCompras() {
    this.compraService.listarCompras().subscribe({
      next: (data) => {
        this.compras = data;
        this.filtrarCompras();
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al traer compras', err);
        this.alertService.error('Error al cargar la lista de compras');
        this.cargando = false;
      }
    });
  }

  filtrarCompras(): void {
    const term = this.terminoBusqueda.trim().toLowerCase();
    if (!term) {
      this.comprasFiltradas = [...this.compras];
      return;
    }

    if (this.tipoBusqueda === 'proveedor') {
      this.comprasFiltradas = this.compras.filter(c =>
        c.razonSocialProveedor?.toLowerCase().includes(term)
      );
    } else {
      this.comprasFiltradas = this.compras.filter(c => {
        const detalles = c.detalles || c.detalleCompra || [];
        return detalles.some(d => d.nombreProducto?.toLowerCase().includes(term));
      });
    }
  }

  resetearBusquedaCompras(): void {
    this.terminoBusqueda = '';
    this.comprasFiltradas = [...this.compras];
  }

  getNombrePrimerProducto(compra: Compra): string {
    const list = compra.detalles || compra.detalleCompra || [];
    return list[0]?.nombreProducto || '';
  }

  getCantidadProductosExtra(compra: Compra): number {
    const list = compra.detalles || compra.detalleCompra || [];
    return list.length > 1 ? list.length - 1 : 0;
  }

  tieneProductos(compra: Compra): boolean {
    const list = compra.detalles || compra.detalleCompra || [];
    return list.length > 0;
  }

  get productosFiltrados(): Producto[] {
    const q = this.busqueda.trim().toLowerCase();
    if (!q) return [];
    return this.productos
      .filter(p => p.nombreProducto?.toLowerCase().includes(q) || (p.codigo ?? '').toLowerCase().includes(q))
      .slice(0, 8);
  }

  get total(): number {
    return this.items.reduce((s, i) => s + i.cantidad * i.precioCompra, 0);
  }

  verDetalles(compra: Compra, index: number): void {
    this.compraSeleccionada = compra;
    this.numeroCompraSeleccionada = index + 1;

    const historialElemento = document.getElementById('comprasModal');
    const detalleElemento = document.getElementById('detalleCompraModal');

    if (!historialElemento || !detalleElemento) {
      return;
    }

    const historialModal = bootstrap.Modal.getOrCreateInstance(historialElemento);
    const detalleModal = bootstrap.Modal.getOrCreateInstance(detalleElemento);

    historialElemento.addEventListener('hidden.bs.modal', () => {
      detalleModal.show();
    }, { once: true });

    detalleElemento.addEventListener('hidden.bs.modal', () => {
      historialModal.show();
    }, { once: true });

    historialModal.hide();
  }

  verEstado(compra: Compra): void {
    this.compraSeleccionada = compra;
    this.nuevoEstadoSeleccionado = compra.estado ?? null;

    const historialElemento = document.getElementById('comprasModal');
    const estadoElemento = document.getElementById('estadoModal');

    if (!historialElemento || !estadoElemento) {
      return;
    }

    const historialModal = bootstrap.Modal.getOrCreateInstance(historialElemento);
    const estadoModal = bootstrap.Modal.getOrCreateInstance(estadoElemento);

    historialElemento.addEventListener('hidden.bs.modal', () => {
      estadoModal.show();
    }, { once: true });

    estadoElemento.addEventListener('hidden.bs.modal', () => {
      historialModal.show();
    }, { once: true });

    historialModal.hide();
  }

  seleccionarProducto(p: Producto): void {
    this.productoSeleccionado = p;
    this.busqueda = '';
  }

  cancelarSeleccion(): void {
    this.productoSeleccionado = null;
    this.limpiarFormularioItem();
  }

  agregarALista(): void {
    if (!this.productoSeleccionado || !this.productoSeleccionado.idProducto) return;
    if (!this.lote.trim() || !this.fechaVencimiento || this.cantidad < 1 || this.precioCompra <= 0) {
      this.alertService.error('Completa lote, fecha de vencimiento, cantidad y precio.');
      return;
    }

    this.items.push({
      idProducto: this.productoSeleccionado.idProducto,
      nombreProducto: this.productoSeleccionado.nombreProducto,
      lote: this.lote.trim(),
      fechaVencimiento: this.fechaVencimiento,
      cantidad: this.cantidad,
      precioCompra: this.precioCompra
    });

    this.cancelarSeleccion();
  }

  eliminarItem(index: number): void {
    this.items.splice(index, 1);
  }

  guardarCompra(): void {
    const usuario = this.authService.getUsuario();

    if (!this.proveedorSeleccionado?.idProveedor) {
      this.alertService.error('Seleccione un proveedor para registrar la compra.');
      return;
    }

    if (this.items.length === 0) {
      this.alertService.error('Agregue al menos un producto a la compra.');
      return;
    }

    if (!usuario?.idUsuario) {
      this.alertService.error('No se pudo identificar al usuario que registra la compra.');
      return;
    }

    const compraData = {
      idProveedor: this.proveedorSeleccionado.idProveedor,
      idUsuario: usuario.idUsuario,
      detalles: this.items.map(item => ({
        idProducto: item.idProducto,
        cantidad: item.cantidad,
        precioCosto: item.precioCompra,
        numeroLote: item.lote,
        fechaVencimiento: item.fechaVencimiento
      }))
    };

    console.log('JSON a enviar al backend:', compraData);
    this.guardando = true;

    this.compraService.crearCompra(compraData).subscribe({
      next: () => {
        (document.activeElement as HTMLElement)?.blur();
        this.alertService.success('Compra creada exitosamente');
        this.cargarCompras();
        this.items = [];
        this.proveedorSeleccionado = null;
        this.productoSeleccionado = null;
        this.busqueda = '';
        this.limpiarFormularioItem();
        this.guardando = false;
      },
      error: (err) => {
        (document.activeElement as HTMLElement)?.blur();
        console.error('Error al crear compra', err);
        this.alertService.error('Error al crear la compra: ' + (err.error?.message || 'Verifique los datos ingresados.'));
        this.guardando = false;
      }
    });
  }

  guardarProveedor() {
    if (this.formProveedor.invalid) {
      this.formProveedor.markAllAsTouched();
      (document.activeElement as HTMLElement)?.blur();
      this.alertService.error('Por favor, complete todos los campos requeridos correctamente.');
      console.error('Formulario proveedor inválido:', this.formProveedor.errors);
      return;
    }

    const proveedorData = {
      razonSocial: this.formProveedor.value.razonSocial,
      telefono: this.formProveedor.value.telefono,
      ruc: this.formProveedor.value.ruc,
      correo: this.formProveedor.value.correo,
      direccion: this.formProveedor.value.direccion,
      estado: 'ACTIVO' as const
    };

    console.log('JSON a enviar al backend:', proveedorData);

    this.proveedorService.crearProveedor(proveedorData).subscribe({
      next: (proveedor) => {
        (document.activeElement as HTMLElement)?.blur();
        this.alertService.success('Proveedor creado exitosamente');
        this.proveedorService.listarProveedores().subscribe(data => {
          this.proveedores = data;
          this.proveedorSeleccionado = proveedor;
        });
        this.formProveedor.reset();
        
        // Cerrar modal de nuevo proveedor
        const btnCerrar = document.querySelector('#proveedorModal [data-bs-dismiss="modal"]') as HTMLElement;
        btnCerrar?.click();
      },
      error: (err) => {
        (document.activeElement as HTMLElement)?.blur();
        console.error('Error al crear proveedor', err);
        this.alertService.error('Error al crear el proveedor: ' + (err.error?.message || 'Verifique los datos'));
      }
    });
  }

  async actualizarEstado(): Promise<void> {
    if (!this.compraSeleccionada?.idCompra || !this.nuevoEstadoSeleccionado) {
      this.alertService.error('Debe seleccionar un estado válido');
      return;
    }
    if (this.nuevoEstadoSeleccionado === this.compraSeleccionada.estado) {
      this.alertService.error('El estado seleccionado es igual al estado actual');
      return;
    }

    const confirmado = await this.alertService.confirm(
      `¿Desea cambiar el estado de la compra de ${this.compraSeleccionada.estado} a ${this.nuevoEstadoSeleccionado}?`,
      'Cambiar estado de compra'
    );

    if (!confirmado) {
      return;
    }

    this.cargando = true;

    this.compraService.actualizarEstadoCompra(this.compraSeleccionada.idCompra, this.nuevoEstadoSeleccionado).subscribe({
      next: () => {
        (document.activeElement as HTMLElement)?.blur();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('estadoModal')).hide();
        this.cargarCompras();
        this.alertService.toast(`Estado de compra actualizado a ${this.nuevoEstadoSeleccionado}.`);
        this.nuevoEstadoSeleccionado = null;
        this.cargando = false;
      },
      error: (err) =>{
        (document.activeElement as HTMLElement)?.blur();
        console.error('Error al actualizar estado:', err);
        this.alertService.error('No se pudo actualizar el estado: ' + (err.error?.message || 'Error en el servidor'));
        this.cargando = false;
      }
    })
  }

  private limpiarFormularioItem(): void {
    this.lote = '';
    this.fechaVencimiento = '';
    this.cantidad = 1;
    this.precioCompra = 0;
  }
}
