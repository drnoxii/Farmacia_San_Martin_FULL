import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaService } from '../../core/services/caja.service';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { Caja, CajaResumen } from '../../core/interfaces/caja.interface';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-caja',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './caja.component.html',
  styleUrl: './caja.component.css'
})
export class CajaComponent implements OnInit {

  idUsuario = 0;          // usuario de la sesión autenticada
  cargando = true;        // true mientras esperamos respuesta del backend

  cajaActual: CajaResumen | null = null;   // caja abierta del usuario (con totales)
  cajaVista: CajaResumen | null = null;    // caja del historial que se está consultando
  historial: Caja[] = [];                  // todas las cajas del usuario

  montoInicial: number | null = null;      // input al abrir caja
  montoContado: number | null = null;      // input al cerrar caja
  montoDigitalVerificado: number | null = null;
  observacionCierre = '';

  modalCierreAbierto = false;
  consultandoCierre = false;
  cerrandoCaja = false;

  cajaParaCierre: CajaResumen | null = null;
  ventaExpandida: string | null = null;    // N° de venta cuyo detalle está desplegado

  constructor(private cajaService: CajaService, private alert: AlertService, private auth: AuthService) { }

  /**
   * QUÉ HACE: al entrar a la pantalla, identifica al usuario y carga los datos.
   * POR QUÉ: la caja pertenece a un usuario; sin su id no sabríamos cuál mostrar.
   */
  ngOnInit(): void {
    const usuario = this.auth.getUsuario();
    if (!usuario) { this.auth.cerrarSesion(); return; }
    this.idUsuario = usuario.idUsuario;
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

private redondear(valor: number): number {
  return Math.round((valor + Number.EPSILON) * 100) / 100;
}

private montoValido(valor: number | null): boolean {
  return valor !== null &&
    Number.isFinite(valor) &&
    valor >= 0 &&
    Math.abs(valor * 100 - Math.round(valor * 100)) < 0.000001;
}

get digitalEsperado(): number {
  const c = this.cajaParaCierre;
  if (!c) return 0;

  return this.redondear(
    c.ventasYape + c.ventasPlin + c.ventasTarjeta
  );
}

get totalEsperado(): number {
  return this.redondear(
    (this.cajaParaCierre?.montoEsperadoEfectivo ?? 0)
    + this.digitalEsperado
  );
}

get totalVerificado(): number | null {
  if (!this.montosValidos) return null;

  return this.redondear(
    this.montoContado! + this.montoDigitalVerificado!
  );
}

get montosValidos(): boolean {
  return this.montoValido(this.montoContado) &&
    this.montoValido(this.montoDigitalVerificado);
}

get diferenciaEfectivo(): number | null {
  if (!this.cajaParaCierre || !this.montoValido(this.montoContado))
    return null;

  return this.redondear(
    this.montoContado! - this.cajaParaCierre.montoEsperadoEfectivo
  );
}

get diferenciaDigital(): number | null {
  if (!this.cajaParaCierre || !this.montoValido(this.montoDigitalVerificado))
    return null;

  return this.redondear(
    this.montoDigitalVerificado! - this.digitalEsperado
  );
}

get diferenciaTotal(): number | null {
  if (this.diferenciaEfectivo === null ||
      this.diferenciaDigital === null) return null;

  return this.redondear(
    this.diferenciaEfectivo + this.diferenciaDigital
  );
}

get hayDescuadre(): boolean {
  return this.diferenciaEfectivo !== null &&
    this.diferenciaDigital !== null &&
    (this.diferenciaEfectivo !== 0 || this.diferenciaDigital !== 0);
}

get estadoArqueo(): 'pendiente' | 'cuadrado' | 'faltante' | 'sobrante' | 'cruzado' {
  const e = this.diferenciaEfectivo;
  const d = this.diferenciaDigital;

  if (e === null || d === null) return 'pendiente';
  if (e === 0 && d === 0) return 'cuadrado';
  if (e * d < 0) return 'cruzado';
  if (e < 0 || d < 0) return 'faltante';

  return 'sobrante';
}

get puedeCerrar(): boolean {
  return this.montosValidos &&
    (!this.hayDescuadre || this.observacionCierre.trim().length > 0);
}


get historialOrdenado(): Caja[] {
  return [...this.historial].sort((a, b) => {

    // Las cajas abiertas quedan al final.
    if (!a.fechaCierre && b.fechaCierre) return 1;
    if (a.fechaCierre && !b.fechaCierre) return -1;

    // Las cajas cerradas se ordenan de más reciente a más antigua.
    const fechaA = new Date(
      a.fechaCierre ?? a.fechaApertura
    ).getTime();

    const fechaB = new Date(
      b.fechaCierre ?? b.fechaApertura
    ).getTime();

    return fechaB - fechaA;
  });
}



abrirModalCierre(): void {
  if (!this.cajaActual || this.consultandoCierre) return;

  this.consultandoCierre = true;

  // Consultamos los montos más recientes antes del arqueo.
  this.cajaService.resumen(this.cajaActual.idCaja).subscribe({
    next: resumen => {
      this.consultandoCierre = false;

      if (resumen.estado !== 'ABIERTA') {
        this.alert.error('Esta caja ya no está abierta.');
        this.cargar();
        return;
      }

      this.cajaActual = resumen;
      this.cajaParaCierre = resumen;

      this.montoContado = null;
      this.montoDigitalVerificado = null;
      this.observacionCierre = '';

      this.modalCierreAbierto = true;
    },
    error: err => {
      this.consultandoCierre = false;
      this.alert.error(
        err.error?.message || 'No se pudo consultar el resumen de caja'
      );
    }
  });
}

cerrarModalCierre(): void {
  if (this.cerrandoCaja) return;

  this.modalCierreAbierto = false;
  this.cajaParaCierre = null;
  this.montoContado = null;
  this.montoDigitalVerificado = null;
  this.observacionCierre = '';
}


async cerrarCaja(): Promise<void> {
  if (!this.cajaParaCierre || !this.puedeCerrar || this.cerrandoCaja)
    return;

  const estado = this.estadoArqueo;

  const config = {
    cuadrado: {
      icon: 'success' as const,
      titulo: 'Caja cuadrada',
      color: '#198754'
    },
    faltante: {
      icon: 'error' as const,
      titulo: 'Se detectó un faltante',
      color: '#dc3545'
    },
    sobrante: {
      icon: 'warning' as const,
      titulo: 'Se detectó un sobrante',
      color: '#e0a800'
    },
    cruzado: {
      icon: 'warning' as const,
      titulo: 'Descuadre entre métodos de pago',
      color: '#e0a800'
    },
    pendiente: {
      icon: 'info' as const,
      titulo: 'Verificar cierre',
      color: '#003A6C'
    }
  }[estado];

  const confirmacion = await Swal.fire({
    icon: config.icon,
    title: config.titulo,
    html: `
      <p>¿Confirmas el cierre definitivo de la caja?</p>
      <p>Total verificado:
        <strong>S/ ${this.totalVerificado?.toFixed(2)}</strong>
      </p>
      <p>Diferencia total:
        <strong>S/ ${this.diferenciaTotal?.toFixed(2)}</strong>
      </p>
      ${this.hayDescuadre
        ? '<p class="text-danger">El descuadre quedará registrado para revisión administrativa.</p>'
        : ''}
    `,
    width: '600px',
    showCancelButton: true,
    confirmButtonText: 'Sí, cerrar caja',
    cancelButtonText: 'Volver',
    confirmButtonColor: config.color,
    reverseButtons: true
  });

  if (!confirmacion.isConfirmed) return;

  this.cerrandoCaja = true;

  this.cajaService.cerrarConArqueo(this.cajaParaCierre.idCaja, {
    idUsuario: this.idUsuario,
    montoContado: this.montoContado!,
    montoDigitalVerificado: this.montoDigitalVerificado!,
    observacionCierre: this.observacionCierre.trim() || null
  }).subscribe({
    next: async caja => {
      this.cerrandoCaja = false;
      this.cerrarModalCierre();
      this.cajaVista = null;
      this.cargar();

      // Mostrar el resultado REAL devuelto por Spring Boot.
      const efectivo = Number(caja.diferencia ?? 0);
      const digital = Number(caja.diferenciaDigital ?? 0);
      const diferencia = Number(caja.diferenciaTotal ?? 0);

      const cruzado = efectivo * digital < 0;
      const cuadrado = efectivo === 0 && digital === 0;
      const faltante = !cruzado && (efectivo < 0 || digital < 0);

      await Swal.fire({
        icon: cuadrado ? 'success' : faltante ? 'error' : 'warning',
        title: cuadrado
          ? '¡Caja cerrada correctamente!'
          : 'Caja cerrada con diferencias',
        text: cuadrado
          ? 'El efectivo y los pagos digitales cuadraron.'
          : `Diferencia total: S/ ${diferencia.toFixed(2)}. El cierre quedó registrado para revisión.`,
        confirmButtonText: 'Aceptar',
        confirmButtonColor: cuadrado ? '#198754' : faltante ? '#dc3545' : '#e0a800'
      });
    },
    error: err => {
      this.cerrandoCaja = false;
      this.cerrarModalCierre();
      this.cargar();

      this.alert.error(
        err.error?.message ||
        err.error?.detail ||
        'No se pudo cerrar la caja'
      );
    }
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
