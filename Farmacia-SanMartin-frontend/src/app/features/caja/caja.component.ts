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

  idUsuario = 0;
  cargando = true;

  cajaActual: CajaResumen | null = null;   // caja abierta del usuario
  cajaVista: CajaResumen | null = null;    // caja del historial que se está consultando
  historial: Caja[] = [];

  montoInicial: number | null = null;
  montoContado: number | null = null;
  ventaExpandida: string | null = null;

  constructor(private cajaService: CajaService, private alert: AlertService) {}

  ngOnInit(): void {
    const guardado = localStorage.getItem('usuario');
    this.idUsuario = guardado ? JSON.parse(guardado).idUsuario : 0;
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.cajaService.cajaAbierta(this.idUsuario).subscribe({
      next: resumen => {
        this.cajaActual = resumen;
        this.cargando = false;
      },
      error: err => {
        this.cargando = false;
        this.alert.error(err.error?.message || 'No se pudo cargar la caja');
      }
    });
    this.cajaService.historial(this.idUsuario).subscribe({
      next: lista => this.historial = [...lista].reverse()
    });
  }

  // ---------- Abrir ----------
  abrirCaja(): void {
    if (this.montoInicial === null || this.montoInicial < 0) {
      this.alert.error('Ingresa un monto inicial válido (no negativo).');
      return;
    }
    this.cajaService.abrir(this.idUsuario, this.montoInicial).subscribe({
      next: () => {
        this.alert.toast('Caja abierta correctamente');
        this.montoInicial = null;
        this.cargar();
      },
      error: err => this.alert.error(err.error?.message || 'No se pudo abrir la caja')
    });
  }

  // ---------- Cerrar ----------
  get diferenciaPrevia(): number | null {
    if (!this.cajaActual || this.montoContado === null) return null;
    return +(this.montoContado - this.cajaActual.montoEsperadoEfectivo).toFixed(2);
  }

  async cerrarCaja(): Promise<void> {
    if (!this.cajaActual) return;
    if (this.montoContado === null || this.montoContado < 0) {
      this.alert.error('Ingresa el monto contado en efectivo.');
      return;
    }
    const ok = await this.alert.confirm(
      'Se cerrará la caja y ya no podrás registrar ventas en ella.',
      '¿Cerrar caja?');
    if (!ok) return;

    this.cajaService.cerrar(this.cajaActual.idCaja, this.idUsuario, this.montoContado).subscribe({
      next: caja => {
        const dif = caja.diferencia ?? 0;
        const msg = dif === 0 ? 'La caja cuadró exactamente.'
          : dif > 0 ? `Sobrante de S/ ${dif.toFixed(2)}`
          : `Faltante de S/ ${Math.abs(dif).toFixed(2)}`;
        this.alert.success(msg, 'Caja cerrada');
        this.montoContado = null;
        this.cajaVista = null;
        this.cargar();
      },
      error: err => this.alert.error(err.error?.message || 'No se pudo cerrar la caja')
    });
  }

  // ---------- Consultas ----------
  verCaja(c: Caja): void {
    this.cajaService.resumen(c.idCaja).subscribe({
      next: r => { this.cajaVista = r; this.ventaExpandida = null; },
      error: err => this.alert.error(err.error?.message || 'No se pudo cargar el detalle')
    });
  }

  toggleVenta(numero: string): void {
    this.ventaExpandida = this.ventaExpandida === numero ? null : numero;
  }

  claseMetodo(m: string): string {
    return { EFECTIVO: 'bg-success', YAPE: 'bg-purple', PLIN: 'bg-info text-dark', TARJETA: 'bg-primary' }[m] || 'bg-secondary';
  }
}