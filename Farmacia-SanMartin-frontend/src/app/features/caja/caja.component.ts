import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaService } from '../../core/services/caja.service';
import { AlertService } from '../../core/services/alert.service';
import { Caja, CajaResumen } from '../../core/interfaces/caja.interface';

@Component({
  selector: 'app-caja',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './caja.component.html',
  styleUrl: './caja.component.css'
})
export class CajaComponent implements OnInit {

  idUsuario = 0;          // usuario logueado (se lee del localStorage)
  cargando = true;        // true mientras esperamos respuesta del backend

  cajaActual: CajaResumen | null = null;   // caja abierta del usuario (con totales)
  cajaVista: CajaResumen | null = null;    // caja del historial que se está consultando
  historial: Caja[] = [];                  // todas las cajas del usuario

  montoInicial: number | null = null;      // input al abrir caja
  montoContado: number | null = null;      // input al cerrar caja
  ventaExpandida: string | null = null;    // N° de venta cuyo detalle está desplegado

  constructor(private cajaService: CajaService, private alert: AlertService) {}

  /**
   * QUÉ HACE: al entrar a la pantalla, identifica al usuario y carga los datos.
   * POR QUÉ: la caja pertenece a un usuario; sin su id no sabríamos cuál mostrar.
   */
  ngOnInit(): void {
    const guardado = localStorage.getItem('usuario');
    this.idUsuario = guardado ? JSON.parse(guardado).idUsuario : 0;
    this.cargar();
  }

  /**
   * QUÉ HACE: trae del backend la caja abierta (con totales) y el historial.
   * POR QUÉ: se llama al iniciar, al abrir/cerrar caja y con el botón "Actualizar"
   *   para que los montos reflejen las ventas más recientes.
   */
  cargar(): void {
    this.cargando = true;

    // 1. Caja abierta con su resumen (totales + ventas).
    this.cajaService.cajaAbierta(this.idUsuario).subscribe({
      next: resumen => {
        this.cajaActual = resumen;   // null si no hay caja abierta
        this.cargando = false;
      },
      error: err => {
        // Ahora un error es un problema real (servidor caído, endpoint roto),
        // NO "no hay caja". Lo mostramos para no confundirlo con caja cerrada.
        console.error('Error cargando caja:', err);
        this.cajaActual = null;
        this.cargando = false;
        this.alert.error(err.error?.message || 'No se pudo cargar la caja');
      }
    });

    // 2. Historial. Se invierte para mostrar la más reciente arriba.
    this.cajaService.historial(this.idUsuario).subscribe({
      next: lista => this.historial = [...lista].reverse(),   // [...] copia para no mutar el original
      error: () => this.historial = []
    });
  }

  // ---------- Abrir ----------

  /**
   * QUÉ HACE: valida el monto inicial y abre una caja nueva.
   * POR QUÉ: un monto vacío o negativo no tiene sentido; se valida antes de
   *   molestar al servidor. Se recarga para mostrar la caja recién creada.
   */
  abrirCaja(): void {
    if (this.montoInicial === null || this.montoInicial < 0) {
      this.alert.error('Ingresa un monto inicial válido (no negativo).');
      return;
    }
    this.cajaService.abrir(this.idUsuario, this.montoInicial).subscribe({
      next: () => {
        this.alert.toast('Caja abierta correctamente');
        this.montoInicial = null;   // limpia el input
        this.cargar();
      },
      error: err => this.alert.error(err.error?.message || 'No se pudo abrir la caja')
    });
  }

  // ---------- Cerrar ----------

  /**
   * QUÉ HACE: calcula en vivo la diferencia entre lo contado y lo esperado.
   * POR QUÉ: así el cajero ve si hay sobrante o faltante ANTES de confirmar.
   *   Es un "getter": Angular lo recalcula cada vez que cambia montoContado.
   *   toFixed(2) y el "+" evitan errores de decimales (ej: 0.1 + 0.2).
   */
  get diferenciaPrevia(): number | null {
    if (!this.cajaActual || this.montoContado === null) return null;
    return +(this.montoContado - this.cajaActual.montoEsperadoEfectivo).toFixed(2);
  }

  /**
   * QUÉ HACE: pide confirmación y cierra la caja con el efectivo contado.
   * POR QUÉ: cerrar es irreversible (ya no se pueden registrar ventas), por eso
   *   se confirma. Al terminar se informa si cuadró, sobró o faltó dinero.
   */
  async cerrarCaja(): Promise<void> {
    if (!this.cajaActual) return;   // no hay caja que cerrar
    if (this.montoContado === null || this.montoContado < 0) {
      this.alert.error('Ingresa el monto contado en efectivo.');
      return;
    }
    const ok = await this.alert.confirm(
      'Se cerrará la caja y ya no podrás registrar ventas en ella.',
      '¿Cerrar caja?');
    if (!ok) return;   // el usuario canceló

    this.cajaService.cerrar(this.cajaActual.idCaja, this.idUsuario, this.montoContado).subscribe({
      next: caja => {
        const dif = caja.diferencia ?? 0;   // ?? usa 0 si viene null
        const msg = dif === 0 ? 'La caja cuadró exactamente.'
          : dif > 0 ? `Sobrante de S/ ${dif.toFixed(2)}`
          : `Faltante de S/ ${Math.abs(dif).toFixed(2)}`;
        this.alert.success(msg, 'Caja cerrada');
        this.montoContado = null;
        this.cajaVista = null;
        this.cargar();   // refresca: ahora no habrá caja abierta
      },
      error: err => this.alert.error(err.error?.message || 'No se pudo cerrar la caja')
    });
  }

  // ---------- Consultas ----------

  /**
   * QUÉ HACE: carga el resumen de una caja del historial (botón del ojo).
   * POR QUÉ: permite revisar ventas de cajas pasadas sin salir de la pantalla.
   */
  verCaja(c: Caja): void {
    this.cajaService.resumen(c.idCaja).subscribe({
      next: r => { this.cajaVista = r; this.ventaExpandida = null; },
      error: err => this.alert.error(err.error?.message || 'No se pudo cargar el detalle')
    });
  }

  /**
   * QUÉ HACE: despliega u oculta el detalle de una venta.
   * POR QUÉ: si hago clic en la que ya está abierta, se cierra (null);
   *   si es otra, se abre esa. Solo hay una expandida a la vez.
   */
  toggleVenta(numero: string): void {
    this.ventaExpandida = this.ventaExpandida === numero ? null : numero;
  }

  /**
   * QUÉ HACE: devuelve la clase CSS (color) de la etiqueta según el método de pago.
   * POR QUÉ: así cada método se distingue visualmente. Si llega uno desconocido,
   *   usa gris (bg-secondary) en vez de romperse.
   */
  claseMetodo(m: string): string {
    return { EFECTIVO: 'bg-success', YAPE: 'bg-purple', PLIN: 'bg-info text-dark', TARJETA: 'bg-primary' }[m] || 'bg-secondary';
  }
}