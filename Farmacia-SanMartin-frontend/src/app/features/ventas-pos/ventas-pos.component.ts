import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
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
import Swal from 'sweetalert2';

@Component({
  selector: 'app-ventas-pos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ventas-pos.component.html',
  styleUrl: './ventas-pos.component.css'
})
export class VentasPosComponent implements OnInit {
  private auth = inject(AuthService);

  private productoService = inject(ProductoService);
  private loteService = inject(LoteService);
  private ventaService = inject(VentaService);
  private alertService = inject(AlertService);
  private cajaService = inject(CajaService);
  private clienteService = inject(ClienteService);
  cartService = inject(CartService);
  private router = inject(Router);

  // ---------- Caja ----------
  idCaja: number | null = null;
  cajaAbierta = false;

  // ---------- Cliente ----------
  dniBusqueda: string = '';
  clienteSeleccionado: Cliente | null = null;
  buscandoCliente: boolean = false;

  nuevoCliente = {
    numeroDocumento: '',
    nombre: '',
    telefono: '',
    direccion: '',
    sexo: '' as '' | 'MASCULINO' | 'FEMENINO'
  };

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
    const usuario = this.auth.getUsuario();
    if (!usuario) { this.auth.cerrarSesion(); return; }

    // Bloquea la pantalla mientras verificamos la caja
    Swal.fire({
      title: 'Verificando caja...',
      text: 'Comprobando el estado de tu caja.',
      allowOutsideClick: false,
      allowEscapeKey: false,
      showConfirmButton: false,
      didOpen: () => Swal.showLoading()
    });

    this.cajaService.cajaAbierta(usuario.idUsuario).subscribe({
      next: (caja) => {
        this.idCaja = caja ? caja.idCaja : null;
        this.cajaAbierta = !!caja;

        if (this.cajaAbierta) {
          // Caja abierta: permitir utilizar el POS
          Swal.close();
        } else {
          // Caja cerrada: bloquear el POS
          this.mostrarAlertaCajaCerrada();
        }
      },

      error: async (err) => {
        if (err.status === 401) { Swal.close(); return; }
        console.error('Error al verificar caja:', err);

        this.idCaja = null;
        this.cajaAbierta = false;

        // Un error de conexión no significa que la caja esté cerrada
        const resultado = await Swal.fire({
          icon: 'error',
          title: 'No se pudo verificar la caja',
          text: 'No podemos habilitar las ventas sin comprobar el estado de la caja.',
          width: '650px',
          allowOutsideClick: false,
          allowEscapeKey: false,
          showCancelButton: false,
          confirmButtonText: 'Ir a Gestión de Caja',
          confirmButtonColor: '#003A6C'
        });

        if (resultado.isConfirmed) {
          await this.router.navigate(['/caja']);
        }
      }
    });
  }

  async mostrarAlertaCajaCerrada(): Promise<void> {

    const resultado = await

      Swal.fire({
        title: '¡CAJA CERRADA!',
        text: 'Debes abrir una caja antes de utilizar el Punto de Venta.',
        icon: 'warning',
        confirmButtonText: 'Ir a Gestión de Caja',
        confirmButtonColor: '#00897B',
        allowOutsideClick: false,
        allowEscapeKey: false
      });

    if (resultado.isConfirmed) {
      await this.router.navigate(['/caja']);
    }
  }

  // ========== CLIENTE ==========


  buscarCliente(): void {
    if (this.buscandoCliente) return;

    const documento = this.dniBusqueda.trim();
    const tipo = this.tipoDocumentoSeleccionado;

    const valido = tipo === 'DNI'
      ? /^\d{8}$/.test(documento)
      : /^\d{11}$/.test(documento);

    if (!valido) {
      this.alertService.error(
        `Ingresa un ${tipo} válido de ${tipo === 'DNI' ? 8 : 11} dígitos`
      );
      return;
    }

    this.buscandoCliente = true;

    // Primero consultar clientes registrados en Oracle.
    this.clienteService.buscarPorDni(documento).subscribe({
      next: (cliente) => {
        this.buscandoCliente = false;
        this.clienteSeleccionado = cliente;
        this.ajustarTipoComprobante(cliente.numeroDocumento);

        this.alertService.toast(
          'Cliente encontrado correctamente',
          'success'
        );
      },

      error: async (err) => {
        // No registrar como nuevo un cliente ante un error del servidor.
        if (err.status !== 404) {
          this.buscandoCliente = false;
          this.alertService.error(
            'No se pudo buscar al cliente. Verifica la conexión.'
          );
          return;
        }

        const confirmado = await this.alertService.confirm(
          'El cliente no está registrado. ¿Deseas registrarlo?',
          'Cliente no encontrado'
        );

        if (!confirmado) {
          this.buscandoCliente = false;
          return;
        }

        // Preparar los datos del registro.
        this.nuevoCliente = {
          numeroDocumento: documento,
          nombre: '',
          telefono: '',
          direccion: '',
          sexo: ''
        };
        // Para RUC mantenemos el registro manual.
        if (tipo === 'RUC') {
          this.buscandoCliente = false;
          this.abrirModalNuevoCliente();
          return;
        }

        // Para DNI consultamos la API de Factiliza.
        this.clienteService.consultarDni(documento).subscribe({
          next: (persona) => {
            this.buscandoCliente = false;

            const nombreCompleto = [
              persona.nombre,
              persona.apellidoPaterno,
              persona.apellidoMaterno
            ]
              .filter((parte): parte is string =>
                typeof parte === 'string' &&
                parte.trim().length > 0
              )
              .join(' ');

            this.nuevoCliente.nombre = nombreCompleto;
            this.nuevoCliente.direccion = persona.direccion ?? '';
            this.nuevoCliente.telefono = persona.telefono ?? '';

            this.abrirModalNuevoCliente();

            this.alertService.toast(
              'Datos obtenidos mediante Factiliza',
              'success'
            );
          },

          error: (error) => {
            this.buscandoCliente = false;
            console.error('Error al consultar DNI:', error);

            // Si falla Factiliza, permitir registro manual.
            this.abrirModalNuevoCliente();

            this.alertService.toast(
              'No se pudo consultar el DNI. Completa los datos manualmente.',
              'info'
            );
          }
        });
      }
    });
  }



  ajustarTipoComprobante(documento: string): void {
    if (documento.length === 11) {
      this.tipoComprobante = 'FACTURA';
    } else if (documento.length === 8) {
      this.tipoComprobante = 'BOLETA';
    }
  }


  private abrirModalNuevoCliente(): void {
    document.getElementById('btnAbrirModalCliente')?.click();
  }




  guardarNuevoCliente(): void {
    if (!this.nuevoCliente.nombre.trim()) {
      this.alertService.error('Ingresa el nombre del cliente');
      return;
    }


    this.clienteService.crearCliente({
      ...this.nuevoCliente,
      sexo: this.tipoDocumentoSeleccionado === 'DNI'
        ? (this.nuevoCliente.sexo || null)
        : null
    }).subscribe({

      next: (cliente) => {
        // Seleccionar automáticamente al cliente registrado
        this.clienteSeleccionado = cliente;

        // Ajustar boleta o factura según su documento
        this.ajustarTipoComprobante(cliente.numeroDocumento);

        // Cerrar el modal
        document.getElementById('btnCerrarModalCliente')?.click();

        this.alertService.success('Cliente registrado correctamente');
      },

      error: (err) => {
        console.error('Error al registrar cliente:', err);

        this.alertService.error(
          err.error?.message ||
          'Error al crear el cliente. Verifica los datos.'
        );
      }
    });
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

    const usuarioLogueado = this.auth.getUsuario();
    if (!usuarioLogueado) { this.auth.cerrarSesion(); return; }

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


  quitarCliente(): void {
    this.clienteSeleccionado = null;
    this.dniBusqueda = '';
    this.tipoComprobante = 'BOLETA';
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
