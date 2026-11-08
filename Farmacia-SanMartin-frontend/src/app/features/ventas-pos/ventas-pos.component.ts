import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertService } from '../../core/services/alert.service';
import { VentaService } from '../../core/services/venta.service';
import { LoteService } from '../../core/services/lote.service';
import { ProductoService } from '../../core/services/producto.service';
import { CartService } from '../../core/services/cart.service';
import { ClienteService } from '../../core/services/cliente.service';
import { CajaService } from '../../core/services/caja.service';
import { VentaRequest } from '../../core/interfaces/venta.interface';
import { Producto } from '../../core/interfaces/producto.interface';
import { VentaResponse } from '../../core/interfaces/venta-response.interface';
import { Lote } from '../../core/interfaces/lote.interface';
import { Cliente } from '../../core/interfaces/cliente.interface';

@Component({
  selector: 'app-ventas-pos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ventas-pos.component.html',
  styleUrl: './ventas-pos.component.css'
})
export class VentasPosComponent implements OnInit {

  private productoService = inject(ProductoService);
  private loteService = inject(LoteService);
  private ventaService = inject(VentaService);
  private alertService = inject(AlertService);
  private cajaService = inject(CajaService);
  private clienteService = inject(ClienteService);
  cartService = inject(CartService);

  // ---------- Caja ----------
  idCaja: number | null = null;
  cajaAbierta = false;

  // ---------- Cliente ----------
  dniBusqueda: string = '';
  clienteSeleccionado: Cliente | null = null;
  buscandoCliente: boolean = false;
  nuevoCliente = { numeroDocumento: '', nombre: '', telefono: '', direccion: '' };
  tipoDocumentoSeleccionado: 'DNI' | 'RUC' = 'DNI';

  // ---------- Productos ----------
  busqueda: string = '';
  productos: Producto[] = [];
  productoSeleccionado: Producto | null = null;
  lotesDisponibles: Lote[] = [];
  cantidadAAgregar: number = 1;
  loteSeleccionado: Lote | null = null;

  // ---------- Venta ----------
  tipoComprobante: 'BOLETA' | 'FACTURA' = 'BOLETA';
  metodoPago: 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TARJETA' = 'EFECTIVO';
  ventaGenerada: VentaResponse | null = null;
  documentoTicket = '00000000';

  ngOnInit(): void {
    this.productoService.listar().subscribe(data => this.productos = data);
    this.cargarCaja();
  }

  // Obtiene la caja ABIERTA del usuario logueado
  cargarCaja(): void {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    this.cajaService.cajaAbierta(usuario.idUsuario).subscribe({
      next: caja => {
        this.idCaja = caja ? caja.idCaja : null;
        this.cajaAbierta = !!caja;
      },
      error: () => {
        this.idCaja = null;
        this.cajaAbierta = false;
      }
    });
  }

  // ========== CLIENTE ==========

  buscarCliente(): void {
    const maxLength = this.tipoDocumentoSeleccionado === 'DNI' ? 8 : 11;
    if (!this.dniBusqueda || this.dniBusqueda.length !== maxLength) {
      this.alertService.error(`El ${this.tipoDocumentoSeleccionado} debe tener exactamente ${maxLength} dígitos`);
      return;
    }

    this.buscandoCliente = true;
    this.clienteService.buscarPorDni(this.dniBusqueda).subscribe({
      next: (cli) => {
        this.clienteSeleccionado = cli;
        this.buscandoCliente = false;
        this.ajustarTipoComprobante(cli.numeroDocumento);
      },
      error: () => {
        this.buscandoCliente = false;
        this.alertService.confirm(
          'Cliente no encontrado. ¿Deseas registrarlo?',
          'Nuevo Cliente'
        ).then((confirmado) => {
          if (confirmado) {
            this.nuevoCliente = {
              numeroDocumento: this.dniBusqueda,
              nombre: '',
              telefono: '',
              direccion: ''
            };
            document.getElementById('btnAbrirModalCliente')?.click();
          }
        });
      }
    });
  }

  // RUC (11) -> FACTURA, DNI (8) -> BOLETA
  ajustarTipoComprobante(documento: string): void {
    if (documento.length === 11) {
      this.tipoComprobante = 'FACTURA';
    } else if (documento.length === 8) {
      this.tipoComprobante = 'BOLETA';
    }
  }

  guardarNuevoCliente(): void {
    this.clienteService.crearCliente(this.nuevoCliente).subscribe({
      next: (cli) => {
        this.clienteSeleccionado = cli;
        this.alertService.success('Cliente registrado');
        this.ajustarTipoComprobante(cli.numeroDocumento);
        document.getElementById('btnCerrarModalCliente')?.click();
      },
      error: (err) => {
        console.error(err);
        this.alertService.error('Error al crear el cliente. Verifica los datos.');
      }
    });
  }

  quitarCliente(): void {
    this.clienteSeleccionado = null;
    this.dniBusqueda = '';
    this.tipoComprobante = 'BOLETA';
  }

  // ========== PRODUCTOS ==========

  get productosFiltrados() {
    if (!this.busqueda) return [];
    return this.productos.filter(p =>
      p.nombreProducto.toLowerCase().includes(this.busqueda.toLowerCase()) ||
      (p.codigo && p.codigo.toLowerCase().includes(this.busqueda.toLowerCase()))
    ).slice(0, 5);
  }

  seleccionarProducto(p: Producto): void {
    this.productoSeleccionado = p;
    this.busqueda = '';
    this.cantidadAAgregar = 1;

    this.loteService.listarPorProducto(p.idProducto!).subscribe(lotes => {
      this.lotesDisponibles = lotes.filter(l => l.stockLote > 0 && l.estado === 'ACTIVO');
      if (this.lotesDisponibles.length === 0) {
        this.alertService.error('Este producto no tiene stock disponible.');
        this.productoSeleccionado = null;
      }
    });
  }

  agregarAlCarrito(): void {
    if (!this.loteSeleccionado || this.cantidadAAgregar <= 0) return;

    if (this.cantidadAAgregar > this.loteSeleccionado.stockLote) {
      this.alertService.error('No puedes vender más de lo que hay en el lote');
      return;
    }

    this.cartService.agregarProducto(this.productoSeleccionado!, this.loteSeleccionado, this.cantidadAAgregar);

    this.productoSeleccionado = null;
    this.loteSeleccionado = null;
  }

  // ========== VENTA ==========

  registrarVenta(): void {
    const carrito = this.cartService.getItems();
    if (carrito.length === 0) {
      this.alertService.error('El carrito está vacío');
      return;
    }

    if (this.idCaja === null) {
      this.alertService.error('Debes abrir una caja antes de vender. Ve a Administración → Gestión de Caja.');
      return;
    }

    const usuarioLogueado = JSON.parse(localStorage.getItem('usuario') || '{}');

    const ventaRequest: VentaRequest = {
      idCliente: this.clienteSeleccionado?.idCliente ?? null,
      idCaja: this.idCaja,
      idUsuario: usuarioLogueado.idUsuario,
      tipoComprobante: this.tipoComprobante,
      metodoPago: this.metodoPago,
      detalles: carrito.map(item => ({
        idLote: item.lote.idLote,
        cantidad: item.cantidad
      }))
    };

    this.ventaService.registrarVenta(ventaRequest).subscribe({
      next: (res) => {
        this.alertService.success('Venta registrada correctamente');
        this.ventaGenerada = res;
        this.documentoTicket = this.clienteSeleccionado?.numeroDocumento ?? '00000000';
        this.cartService.vaciarCarrito();
        this.quitarCliente();
        document.getElementById('btnAbrirTicket')?.click();
      },
      error: (err) => {
        this.alertService.error(err.error?.message || 'Error al registrar la venta');
      }
    });
  }

  imprimirTicket(): void {
    if (!this.ventaGenerada) {
      this.alertService.error('No hay ticket para imprimir');
      return;
    }
    setTimeout(() => {
      window.print();
    }, 300);
  }
}