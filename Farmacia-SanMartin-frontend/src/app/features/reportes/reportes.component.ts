import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../core/services/reporte.service';
import { ExportService, Hoja } from '../../core/services/export.service';
import { AlertService } from '../../core/services/alert.service';
import { ReporteCierres, ReporteInventario, ReporteVentas, ResumenVentasSP } from '../../core/interfaces/reporte.interface';


type Tab = 'ventas' | 'inventario' | 'cierres';
type Periodo = 'hoy' | 'semana' | 'mes' | 'rango';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css'
})
export class ReportesComponent implements OnInit {

  private reporteService = inject(ReporteService);
  private exportService = inject(ExportService);
  private alert = inject(AlertService);

  tab: Tab = 'ventas';
  periodo: Periodo = 'hoy';
  desde = '';
  hasta = '';
  dias = 30;
  cargando = false;

  ventas: ReporteVentas | null = null;
  inventario: ReporteInventario | null = null;
  cierres: ReporteCierres | null = null;
  resumenSP: ResumenVentasSP | null = null;
  cargandoResumenSP = false;
  errorResumenSP = '';


  ngOnInit(): void {
    this.setPeriodo('hoy');
  }

  // ---------- Filtros ----------
  setPeriodo(p: Periodo): void {
    this.periodo = p;
    const hoy = new Date();
    if (p === 'hoy') {
      this.desde = this.hasta = this.iso(hoy);
    } else if (p === 'semana') {
      const lunes = new Date(hoy);
      lunes.setDate(hoy.getDate() - ((hoy.getDay() + 6) % 7));
      this.desde = this.iso(lunes);
      this.hasta = this.iso(hoy);
    } else if (p === 'mes') {
      this.desde = this.iso(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
      this.hasta = this.iso(hoy);
    }
    if (p !== 'rango') this.cargar();
  }

  cambiarTab(t: Tab): void {
    this.tab = t;
    this.cargar();
  }

  cargar(): void {
    if (this.tab !== 'inventario') {
      if (!this.desde || !this.hasta) {
        this.alert.error('Selecciona las fechas.');
        return;
      }
      if (this.desde > this.hasta) {
        this.alert.error('La fecha "desde" no puede ser mayor que "hasta".');
        return;
      }
    }

    this.cargando = true;
    const fin = () => (this.cargando = false);
    const fallo = (err: any) => {
      fin();
      this.alert.error(err.error?.message || 'No se pudo cargar el reporte');
    };

    
if (this.tab === 'ventas') {
  const desde = this.desde;
  const hasta = this.hasta;

  this.resumenSP = null;
  this.errorResumenSP = '';
  this.cargandoResumenSP = true;

  this.reporteService.ventas(desde, hasta).subscribe({
    next: r => {
      this.ventas = r;
      fin();

      this.reporteService.resumenVentasSP(desde, hasta).subscribe({
        next: (resultado: ResumenVentasSP) => {
          this.resumenSP = resultado;
          this.cargandoResumenSP = false;
        },
        error: () => {
          this.cargandoResumenSP = false;
          this.errorResumenSP =
            'No se pudo consultar el procedimiento almacenado.';
        }
      });
    },
    error: err => {
      this.cargandoResumenSP = false;
      fallo(err);
    }
  });
}else if (this.tab === 'inventario') {
      this.reporteService.inventario(this.dias)
        .subscribe({ next: r => { this.inventario = r; fin(); }, error: fallo });
    } else {
      this.reporteService.cierres(this.desde, this.hasta)
        .subscribe({ next: r => { this.cierres = r; fin(); }, error: fallo });
    }
  }

  get hayDatos(): boolean {
    if (this.tab === 'ventas') return !!this.ventas && this.ventas.ventas.length > 0;
    if (this.tab === 'inventario') return !!this.inventario;
    return !!this.cierres && this.cierres.cierres.length > 0;
  }

  // ---------- Formato de fechas (sin desfase de zona horaria) ----------
  fechaCorta(s: string): string {
    return s ? s.substring(0, 10).split('-').reverse().join('/') : '-';
  }

  fechaHora(s: string): string {
    return s ? `${this.fechaCorta(s)} ${s.substring(11, 16)}` : '-';
  }

  diasVencido(n: number): number {
    return Math.abs(n);
  }

  private iso(d: Date): string {
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${dia}`;
  }

  // ---------- Exportar ----------
  private get nombreArchivo(): string {
    const hoy = this.iso(new Date());
    return `reporte_${this.tab}_${hoy}`;
  }

  private get titulo(): string {
    return { ventas: 'Reporte de ventas', inventario: 'Reporte de inventario', cierres: 'Reporte de cierres de caja' }[this.tab];
  }

  private get subtitulo(): string {
    return this.tab === 'inventario'
      ? `Al ${this.fechaCorta(this.iso(new Date()))} · lotes por vencer en ${this.dias} días`
      : `Del ${this.fechaCorta(this.desde)} al ${this.fechaCorta(this.hasta)}`;
  }

  private hojas(): Hoja[] {
    if (this.tab === 'ventas' && this.ventas) {
      const v = this.ventas;
      return [
        {
          nombre: 'Resumen',
          columnas: ['Concepto', 'Cantidad', 'Total (S/)'],
          filas: [
            ['Total vendido', v.cantidadVentas, v.totalVendido],
            ['Ventas anuladas', v.cantidadAnuladas, v.totalAnulado],
            ...v.porMetodoPago.map(x => [`Pago: ${x.etiqueta}`, x.cantidad, x.total]),
            ...v.porComprobante.map(x => [`Comprobante: ${x.etiqueta}`, x.cantidad, x.total]),
            ...v.porVendedor.map(x => [`Vendedor: ${x.etiqueta}`, x.cantidad, x.total])
          ],
          dinero: [2]
        },
        {
          nombre: 'Ventas',
          columnas: ['N° Venta', 'Fecha', 'Cliente', 'Vendedor', 'Comprobante', 'Pago', 'Total (S/)', 'Estado'],
          filas: v.ventas.map(x => [x.numeroVenta, this.fechaHora(x.fecha), x.cliente, x.vendedor,
            x.tipoComprobante, x.metodoPago, x.total, x.estado]),
          dinero: [6]
        }
      ];
    }

    if (this.tab === 'inventario' && this.inventario) {
      const i = this.inventario;
      const lote = (l: any) => [l.codigo, l.producto, l.numeroLote, this.fechaCorta(l.fechaVencimiento), l.stock, l.diasRestantes];
      const colsLote = ['Código', 'Producto', 'Lote', 'Vencimiento', 'Stock', 'Días'];
      return [
        {
          nombre: 'Stock bajo el mínimo',
          columnas: ['Código', 'Producto', 'Categoría', 'Stock actual', 'Stock mínimo', 'Faltan'],
          filas: i.stockBajo.map(s => [s.codigo, s.producto, s.categoria, s.stockActual, s.stockMinimo, s.faltante])
        },
        { nombre: `Lotes por vencer (${i.dias} días)`, columnas: colsLote, filas: i.lotesPorVencer.map(lote) },
        { nombre: 'Lotes vencidos con stock', columnas: colsLote, filas: i.lotesVencidos.map(lote) }
      ];
    }

    if (this.tab === 'cierres' && this.cierres) {
      return [{
        nombre: 'Cierres de caja',
        columnas: ['Cajero', 'Apertura', 'Cierre', 'Inicial', 'Ventas efectivo', 'Esperado', 'Contado', 'Diferencia'],
        filas: this.cierres.cierres.map(c => [c.cajero, this.fechaHora(c.apertura), this.fechaHora(c.cierre),
          c.montoInicial, c.ventasEfectivo, c.esperado, c.contado, c.diferencia]),
        dinero: [3, 4, 5, 6, 7]
      }];
    }
    return [];
  }

  async exportarExcel(): Promise<void> {
    try {
      await this.exportService.excel(this.nombreArchivo, this.hojas());
    } catch {
      this.alert.error('No se pudo generar el Excel.');
    }
  }

  async exportarPdf(): Promise<void> {
    try {
      await this.exportService.pdf(this.nombreArchivo, this.titulo, this.subtitulo, this.hojas());
    } catch {
      this.alert.error('No se pudo generar el PDF.');
    }
  }
}