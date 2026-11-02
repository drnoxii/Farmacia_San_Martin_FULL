import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ComprasService } from '../../../core/services/compras.service';
import { AlertService } from '../../../core/services/alert.service';
import { Compra, EstadoCompra } from '../../../core/interfaces/compras.interface';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { Proveedor } from '../../../core/interfaces/proveedor.interface';
import { ProductoService } from '../../../core/services/producto.service';
import { Producto } from '../../../core/interfaces/producto.interface';

@Component({
  selector: 'app-compras-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ReactiveFormsModule],
  templateUrl: './compras-list.component.html',
  styleUrl: './compras-list.component.css'
})
export class ComprasListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private compraService = inject(ComprasService);
  private alertService = inject(AlertService);
  private proveedorService = inject(ProveedorService);
  private productoService = inject(ProductoService);

  busqueda: string = '';
  cargando: boolean = true;

  compras: Compra[] = [];
  proveedor: Proveedor[] = [];
  productos: Producto[] = [];
  comprasFiltradas: Compra[] = [];

  nuevoEstadoSeleccionado: EstadoCompra | null = null;
  compraSeleccionada: Compra | null = null;
  id : number | null = null;
  formCompra: FormGroup;
  formProveedor: FormGroup;

  terminoBusqueda: string = '';
  tipoBusqueda: 'proveedor' | 'id' = 'proveedor';
  
  constructor() {
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
  this.formProveedor = this.fb.group({
    razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
    telefono: ['', [Validators.required, Validators.pattern('[0-9]{9}')]],
    ruc: ['', [Validators.required, Validators.pattern('[0-9]{11}')]],
    correo: ['', [Validators.required, Validators.email]],
    direccion: ['', Validators.required],
    estado: ['', Validators.required]
  });
}

  ngOnInit(): void {
    this.cargarCompras();
    this.proveedorService.listarProveedores().subscribe(data => {
      this.proveedor = data;
    });
    this.productoService.listar().subscribe(data => {
      this.productos = data;
    });
  }

  cargarCompras() {
    this.compraService.listarCompras().subscribe({
      next: (data) => {
        this.compras = data;
        this.comprasFiltradas = data; // Inicialmente, todas las compras
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

  verEstado(Compra: Compra) {
    this.compraSeleccionada = Compra;
    this.nuevoEstadoSeleccionado = null;
  }


  buscarCompras() {
    const termino = this.terminoBusqueda.trim().toLowerCase();
    if (termino === '') {
      this.comprasFiltradas = this.compras; // Si no hay término, mostrar todas las compras
      return;
    }
    if (this.tipoBusqueda === 'id') {
      console.log('Buscando por ID:', termino);
      this.comprasFiltradas= this.compras.filter(c => 
        c.idCompra?.toString() === termino);
    }else{
      console.log('Buscando por proveedor:', termino);
      this.comprasFiltradas = this.compras.filter(c => 
        c.razonSocialProveedor?.toLowerCase().includes(termino));
    }

  }

  guardarCompra() { 
    if(this.formCompra.invalid) {
      (document.activeElement as HTMLElement)?.blur();
      this.alertService.error('Por favor, complete todos los campos requeridos correctamente.');
      console.error('Formulario inválido:', this.formCompra.errors);
      return;
    }

    const compraData = {
    idProveedor: Number(this.formCompra.value.proveedor),
    idUsuario: Number(this.formCompra.value.usuario) || 1, // ID numérico (ej: 1)
    detalles: [
      {
        idProducto: Number(this.formCompra.value.productos),
        cantidad: Number(this.formCompra.value.cantidad),
        precioCosto: Number(this.formCompra.value.precio),
        numeroLote: this.formCompra.value.lote,
        fechaVencimiento: this.formCompra.value.fechaVencimiento || '2028-12-31' // Obligatorio formato YYYY-MM-DD
      }
    ]
  };

    console.log('JSON a enviar al backend:', compraData);

    this.compraService.crearCompra(compraData).subscribe({
      next: (compra) => {
        (document.activeElement as HTMLElement)?.blur();
        this.alertService.success('Compra creada exitosamente');
        document.getElementById('closeModalButton')?.click();
        this.cargarCompras();
        this.formCompra.reset();
      },
      error: (err) => {
        (document.activeElement as HTMLElement)?.blur();
        console.error('Error al crear compra', err);
        this.alertService.error('Error al crear la compra');
        this.formCompra.reset();
      }
    });
  }

  guardarProveedor() {
    if (this.formProveedor.invalid) {
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
      estado: this.formProveedor.value.estado
    };

    console.log('JSON a enviar al backend:', proveedorData);

    this.proveedorService.crearProveedor(proveedorData).subscribe({
      next: (proveedor) => {
        (document.activeElement as HTMLElement)?.blur();
        this.alertService.success('Proveedor creado exitosamente');
        this.proveedorService.listarProveedores().subscribe(data => {
          this.proveedor = data;
        });
        this.formProveedor.reset();
        
        // Cerrar modal de nuevo proveedor
        const btnCerrar = document.querySelector('#modalNuevoProveedor [data-bs-dismiss="modal"]') as HTMLElement;
        btnCerrar?.click();
      },
      error: (err) => {
        (document.activeElement as HTMLElement)?.blur();
        console.error('Error al crear proveedor', err);
        this.alertService.error('Error al crear el proveedor: ' + (err.error?.message || 'Verifique los datos'));
      }
    });
  }

  actualizarEstado():void {
    if (!this.compraSeleccionada?.idCompra || !this.nuevoEstadoSeleccionado) {
      this.alertService.error('Debe seleccionar un estado válido');
      return;
    }
    if (this.nuevoEstadoSeleccionado === this.compraSeleccionada.estado) {
      this.alertService.error('El estado seleccionado es igual al estado actual');
      return;
    }

    this.cargando = true;

    this.compraService.actualizarEstadoCompra(this.compraSeleccionada.idCompra, this.nuevoEstadoSeleccionado).subscribe({
      next: (compraActualizada) => {
        (document.activeElement as HTMLElement)?.blur();
        this.alertService.success(`Estado de compra actualizado exitosamente a ${this.nuevoEstadoSeleccionado} con éxito`);
        const  btnCerrar = document.querySelector('#modalCambiarEstado [data-bs-dismiss="modal"]') as HTMLElement;
        btnCerrar?.click();
        this.cargarCompras();
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

  calcularTotal() {
    const cantidad = this.formCompra.get('cantidad')?.value || 0;
    const precio = this.formCompra.get('precio')?.value || 0;
    const total = cantidad * precio;
    this.formCompra.get('total')?.setValue(Number(total.toFixed(2))); // Redondea a 2 decimales
  }

  limpiarBusqueda() {
  this.terminoBusqueda = '';
  this.comprasFiltradas = [...this.compras];
  }

}
