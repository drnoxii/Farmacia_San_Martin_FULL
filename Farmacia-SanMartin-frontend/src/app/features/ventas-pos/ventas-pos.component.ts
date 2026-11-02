import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AlertService } from '../../core/services/alert.service';
import { VentaService } from '../../core/services/venta.service';
import { LoteService } from '../../core/services/lote.service';
import { ProductoService } from '../../core/services/producto.service';
import { VentaRequest } from '../../core/interfaces/venta.interface';
import { Producto } from '../../core/interfaces/producto.interface';
import { VentaResponse } from '../../core/interfaces/venta-response.interface';
import { Lote } from '../../core/interfaces/lote.interface';
import { CartService } from '../../core/services/cart.service';
import { ClienteService } from '../../core/services/cliente.service';
import { Cliente } from '../../core/interfaces/cliente.interface';

@Component({
  selector: 'app-ventas-pos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ventas-pos.component.html',
  styleUrl: './ventas-pos.component.css'
})
export class VentasPosComponent {

  private productoService = inject(ProductoService);
  private loteService = inject(LoteService);
  private ventaService = inject(VentaService);
  private alertService = inject(AlertService);


  private clienteService = inject(ClienteService);

  // Variables para cliente
  dniBusqueda: string = '';
  clienteSeleccionado: Cliente | null = null;
  buscandoCliente: boolean = false;
   nuevoCliente = { numeroDocumento: '', nombre: '', telefono: '', direccion: '' };
   tipoDocumentoSeleccionado: 'DNI' | 'RUC' = 'DNI';

  cartService = inject(CartService);

  busqueda: string = '';
  productos: Producto[] = [];

  productoSeleccionado: Producto | null = null;
  lotesDisponibles: Lote[] = [];
  cantidadAAgregar: number = 1;
  loteSeleccionado: Lote | null = null;

  tipoComprobante: 'BOLETA' | 'FACTURA' = 'BOLETA';
  metodoPago: 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TARJETA' = 'EFECTIVO';
  idCliente: number | null = null;

  ventaGenerada: VentaResponse | null = null;

  ngOnInit() {
    this.productoService.listar().subscribe(data => this.productos = data);
  }

  // solo de cliente 

buscarCliente() {
    // Validamos estrictamente que sea 8 o 11 caracteres
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

  // 👇 ESTA ES LA MAGIA: Si es RUC (11), pone FACTURA. Si es DNI (8), pone BOLETA.
  ajustarTipoComprobante(documento: string) {
    if (documento.length === 11) {
      this.tipoComprobante = 'FACTURA';
    } else if (documento.length === 8) {
      this.tipoComprobante = 'BOLETA';
    }
  }

  guardarNuevoCliente() {
    // Le pasamos el objeto exacto que pide la interfaz
    this.clienteService.crearCliente(this.nuevoCliente).subscribe({
      next: (cli) => {
        this.clienteSeleccionado = cli;
        this.alertService.success('Cliente registrado');
        this.ajustarTipoComprobante(cli.numeroDocumento); // Ajusta boleta o factura
        document.getElementById('btnCerrarModalCliente')?.click();
      },
      error: (err) => {
        console.error(err);
        this.alertService.error('Error al crear el cliente. Verifica los datos.');
      }
    });
  }

  quitarCliente() {
    this.clienteSeleccionado = null;
    this.dniBusqueda = '';
    this.tipoComprobante = 'BOLETA'; // Vuelve a la boleta anónima por defecto
  }


  get productosFiltrados() {
    if (!this.busqueda) return [];
    return this.productos.filter(p =>
      p.nombreProducto.toLowerCase().includes(this.busqueda.toLowerCase()) ||
      (p.codigo && p.codigo.toLowerCase().includes(this.busqueda.toLowerCase()))
    ).slice(0, 5);
  }

  seleccionarProducto(p: Producto) {
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

  agregarAlCarrito() {
    if (!this.loteSeleccionado || this.cantidadAAgregar <= 0) return;

    if (this.cantidadAAgregar > this.loteSeleccionado.stockLote) {
      this.alertService.error('No puedes vender más de lo que hay en el lote');
      return;
    }

    // 👇 USAMOS EL SERVICIO DEL CARRITO
    this.cartService.agregarProducto(this.productoSeleccionado!, this.loteSeleccionado, this.cantidadAAgregar);

    this.productoSeleccionado = null;
    this.loteSeleccionado = null;
  }

  // Quitar y actualizar cantidad los maneja directamente el CartService en el HTML

  registrarVenta() {
    const carrito = this.cartService.getItems();
    if (carrito.length === 0) {
      this.alertService.error('El carrito está vacío');
      return;
    }

    const usuarioLogueado = JSON.parse(localStorage.getItem('usuario') || '{}');
    const idCajaAbierta = 1; // Ajustar según tu lógica de caja

    const ventaRequest: VentaRequest = {
      idCliente: this.idCliente,
      idCaja: idCajaAbierta,
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
        this.cartService.vaciarCarrito(); // 👈 VACIAMOS EL CARRITO
        document.getElementById('btnAbrirTicket')?.click();
      },
      error: (err) => {
        this.alertService.error(err.error?.message || 'Error al registrar la venta');
      }
    });
  }

   imprimirTicket() {
    if (!this.ventaGenerada) {
      this.alertService.error('No hay ticket para imprimir');
      return;
    }
    setTimeout(() => {
      window.print();
    }, 300); 
}
}
