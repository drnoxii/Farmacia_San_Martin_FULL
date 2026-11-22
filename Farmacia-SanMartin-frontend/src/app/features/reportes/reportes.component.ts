import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { firstValueFrom, Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../core/services/reporte.service';
import { ExportService, Hoja } from '../../core/services/export.service';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { VentaService } from '../../core/services/venta.service';
import { VentaResponse } from '../../core/interfaces/venta-response.interface';
import { DevolucionVentaResponse } from '../../core/interfaces/devolucion-venta.interface';
import { ReporteCierres, ReporteInventario, ReporteVentas, ResumenVentasSP, ResumenVentas, PaginaVentas } from '../../core/interfaces/reporte.interface';
import { MovimientoInventarioService } from '../../core/services/movimiento-inventario.service';
import { MovimientoInventario, PaginaMovimientos } from '../../core/interfaces/movimiento-inventario.interface';


type Tab = 'ventas' | 'inventario' | 'cierres' | 'kardex';
type Periodo = 'hoy' | 'semana' | 'mes' | 'rango';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css'
})
export class ReportesComponent implements OnInit, OnDestroy {
  private auth = inject(AuthService);

  get esAdministrador(): boolean {
    return this.auth.getUsuario()?.rol === 'ADMIN';
  }

  private reporteService = inject(ReporteService);
  private exportService = inject(ExportService);
  private alert = inject(AlertService);
  private ventaService = inject(VentaService);

  private movimientoService = inject(MovimientoInventarioService);

  kardex: PaginaMovimientos | null = null;
  cargandoKardex = false;
  errorKardex = '';

  ventaEditar: VentaResponse | null = null;
  cargandoVenta = false;
  guardandoDevolucion = false;
  resultadoDevolucion: DevolucionVentaResponse | null = null;
  observacionDevolucion = '';

  editarDevolucion(id: number): void {
    if (this.cargandoVenta || this.guardandoDevolucion) return;
    this.cargandoVenta = true;
    this.resultadoDevolucion = null;
    this.observacionDevolucion = '';
    this.ventaService.buscarVenta(id).subscribe({
      next: venta => {
        this.ventaEditar = venta;
        this.cargandoVenta = false;
      },
      error: () => {
        this.cargandoVenta = false;
        this.alert.error('No se pudo cargar la venta.');
      }
    });
  }

  get detallesADevolver(): number[] {
    return this.ventaEditar?.detalles
      .filter(d => d.estado === 'V' && this.ventaEditar?.estado === 'EMITIDA')
      .map(d => d.idDetalleVenta) ?? [];
  }

  get importeDevolucion(): number {
    return this.ventaEditar?.detalles
      .filter(d => d.estado === 'V' && this.ventaEditar?.estado === 'EMITIDA')
      .reduce((total, d) => total + d.cantidad * d.precioUnitario, 0) ?? 0;
  }

  async guardarDevolucion(): Promise<void> {
    if (!this.ventaEditar || this.guardandoDevolucion || !this.detallesADevolver.length) return;
    const observacion = this.observacionDevolucion.trim();

    if (!observacion || observacion.length > 500) {
      this.alert.error('Ingresa un motivo de devolución válido.');
      return;
    }
    const venta = this.ventaEditar;
    const idDetalles = this.detallesADevolver;
    this.guardandoDevolucion = true;
    const confirmado = await this.alert.confirm(
      `Se devolverán todos los productos pendientes de ${venta.numeroVenta} por S/ ${this.importeDevolucion.toFixed(2)}. Sus cantidades regresarán al stock y la venta quedará anulada.`,
      'Confirmar devolución');
    if (!confirmado) { this.guardandoDevolucion = false; return; }
    this.ventaService.devolver(
      venta.idVenta,
      idDetalles,
      observacion
    ).subscribe({
      next: resultado => {
        this.resultadoDevolucion = resultado;
        venta.detalles.filter(d => idDetalles.includes(d.idDetalleVenta)).forEach(d => d.estado = 'D');
        if (venta.detalles.every(d => d.estado === 'D')) venta.estado = 'ANULADA';
        this.guardandoDevolucion = false;
        this.inventario = null;
        this.cierres = null;
        this.cargar();
      },
      error: err => {
        this.guardandoDevolucion = false;
        this.alert.error(err.error?.detail || err.error?.message ||
          'No se pudo registrar la devolución. Cierra y vuelve a abrir la venta para actualizar su estado.');
      }
    });
  }

  tab: Tab = 'ventas';
  periodo: Periodo = 'hoy';
  desde = '';
  hasta = '';
  dias = 30;
  cargando = false;

  ventas: ResumenVentas | null = null;
  paginaVentas: PaginaVentas | null = null;
  busqueda = '';
  busquedaAplicada = '';
  cargandoDetalle = false;
  errorDetalle = '';
  exportando = false;
  private consultas = new Subscription();
  private consultaDetalle?: Subscription;
  inventario: ReporteInventario | null = null;
  cierres: ReporteCierres | null = null;
  resumenSP: ResumenVentasSP | null = null;
  cargandoResumenSP = false;
  errorResumenSP = '';


  ngOnInit(): void {
    this.setPeriodo('hoy');
  }

  ngOnDestroy(): void {
    this.consultas.unsubscribe();
    this.consultaDetalle?.unsubscribe();
  }

  buscarVentas(): void {
    this.busquedaAplicada = this.busqueda.trim();
    this.cargarPagina(0);
  }

  limpiarBusqueda(): void {
    this.busqueda = '';
    this.buscarVentas();
  }

  cargarPagina(pagina: number): void {
    if (!this.ventas || pagina < 0) return;
    this.consultaDetalle?.unsubscribe();
    this.cargandoDetalle = true;
    this.errorDetalle = '';
    this.consultaDetalle = this.reporteService.detalleVentas(
      this.ventas.desde, this.ventas.hasta, this.busquedaAplicada, pagina
    ).subscribe({
      next: resultado => { this.paginaVentas = resultado; this.cargandoDetalle = false; },
      error: () => {
        this.paginaVentas = null;
        this.cargandoDetalle = false;
        this.errorDetalle = 'No se pudo cargar la lista de ventas. Vuelve a pulsar Buscar.';
      }
    });
  }

  get primerResultado(): number {
    const p = this.paginaVentas;
    return p && p.totalRegistros > 0 ? p.pagina * p.tamano + 1 : 0;
  }

  get ultimoResultado(): number {
    const p = this.paginaVentas;
    return p && p.totalRegistros > 0 ? p.pagina * p.tamano + p.ventas.length : 0;
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

    if (this.tab === 'kardex') {
      this.cargarKardex(0);
      return;
    }
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

    this.consultas.unsubscribe();
    this.consultas = new Subscription();
    this.consultaDetalle?.unsubscribe();
    this.cargandoDetalle = false;
    this.cargando = true;
    const fin = () => (this.cargando = false);
    const fallo = (err: any) => {
      fin();
      this.alert.error(err.error?.message || 'No se pudo cargar el reporte');
    };


    if (this.tab === 'ventas') {
      const desde = this.desde;
      const hasta = this.hasta;
      this.ventas = null;
      this.paginaVentas = null;
      this.resumenSP = null;
      this.errorResumenSP = '';
      this.cargandoResumenSP = true;
      this.consultas.add(this.reporteService.resumenVentas(desde, hasta).subscribe({
        next: r => {
          this.ventas = r;
          fin();
          this.cargarPagina(0);
        },
        error: fallo
      }));
      this.consultas.add(this.reporteService.resumenVentasSP(desde, hasta).subscribe({
        next: r => { this.resumenSP = r; this.cargandoResumenSP = false; },
        error: () => {
          this.cargandoResumenSP = false;
          this.errorResumenSP = 'No se pudo consultar el resumen general.';
        }
      }));
    } else if (this.tab === 'inventario') {
      this.consultas.add(this.reporteService.inventario(this.dias)
        .subscribe({ next: r => { this.inventario = r; fin(); }, error: fallo }));
    } else {
      this.consultas.add(this.reporteService.cierres(this.desde, this.hasta)
        .subscribe({ next: r => { this.cierres = r; fin(); }, error: fallo }));
    }
  }

  get hayDatos(): boolean {
    if (this.tab === 'ventas') return !!this.ventas && this.ventas.totalRegistros > 0;
    if (this.tab === 'inventario') return !!this.inventario;
    if (this.tab === 'kardex') {
      return (this.kardex?.totalElements ?? 0) > 0;
    }
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
    return {
      ventas: 'Reporte de ventas',
      inventario: 'Reporte de inventario',
      cierres: 'Reporte de cierres de caja',
      kardex: 'Kardex de inventario'
    }[this.tab];
  }

  private get subtitulo(): string {
    if (this.tab === 'kardex') {
      return 'Historial completo de movimientos de inventario';
    }
    return this.tab === 'inventario'
      ? `Al ${this.fechaCorta(this.iso(new Date()))} · lotes por vencer en ${this.dias} días`
      : `Del ${this.fechaCorta(this.desde)} al ${this.fechaCorta(this.hasta)}`;
  }

  private hojas(ventasCompletas?: ReporteVentas): Hoja[] {
    if (ventasCompletas) {
      const v = ventasCompletas;
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
          columnas: ['N° Venta', 'Fecha', 'Cliente', 'Vendedor', 'Comprobante', 'Pago', 'Total original (S/)', 'Devuelto (S/)', 'Estado'],
          filas: v.ventas.map(x => [x.numeroVenta, this.fechaHora(x.fecha), x.cliente, x.vendedor,
          x.tipoComprobante, x.metodoPago, x.total, x.totalDevuelto, x.estado]),
          dinero: [6, 7]
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
      const reporte = this.cierres;

      return [
        {
          nombre: 'Resumen',
          columnas: ['Indicador', 'Resultado'],
          filas: [
            ['Periodo',
              `${this.fechaCorta(this.desde)} - ${this.fechaCorta(this.hasta)}`],
            ['Cajas cerradas', reporte.cantidad],
            ['Total sobrantes',
              `S/ ${Number(reporte.totalSobrantes).toFixed(2)}`],
            ['Total faltantes',
              `S/ ${Number(reporte.totalFaltantes).toFixed(2)}`]
          ]
        },
        {
          nombre: 'Detalle de cierres',
          columnas: [
            'Cajero',
            'Apertura',
            'Cierre',
            'Monto inicial',
            'Ventas efectivo',
            'Efectivo esperado',
            'Efectivo contado',
            'Digital esperado',
            'Digital verificado',
            'Dif. efectivo',
            'Dif. digital',
            'Dif. total'
          ],
          filas: reporte.cierres.map(c => [
            c.cajero,
            this.fechaHora(c.apertura),
            this.fechaHora(c.cierre),
            c.montoInicial,
            c.ventasEfectivo,
            c.efectivoEsperadoCierre ?? c.esperado,
            c.contado,
            c.digitalEsperado ?? 'Sin registro',
            c.digitalVerificado ?? 'Sin registro',
            c.diferencia,
            c.diferenciaDigital ?? 'Sin registro',
            c.diferenciaTotal ?? c.diferencia
          ]),
          dinero: [3, 4, 5, 6, 7, 8, 9, 10, 11]
        }
      ];
    }

    return [];
  }

  async exportarExcel(): Promise<void> {
    await this.exportar('excel');
  }

  async exportarPdf(): Promise<void> {
    await this.exportar('pdf');
  }

  private async exportar(formato: 'excel' | 'pdf'): Promise<void> {
    if (this.exportando || this.cargando || !this.hayDatos) return;
    this.exportando = true;
    const nombre = this.nombreArchivo;
    const titulo = this.titulo;
    const subtitulo = this.tab === 'ventas' && this.ventas
      ? `Del ${this.fechaCorta(this.ventas.desde)} al ${this.fechaCorta(this.ventas.hasta)}` : this.subtitulo;
    const resumen = this.tab === 'ventas' ? this.ventas : null;
    const hojasActuales = resumen ? [] : this.hojas();
    try {
      // La exportación incluye todo el período, no solo la página o búsqueda visibles.

      const hojas = this.tab === 'kardex'

        ? this.hojasKardex(
          await this.obtenerTodosLosMovimientos()
        )

        : resumen

          ? this.hojas(
            await firstValueFrom(
              this.reporteService.ventas(
                resumen.desde,
                resumen.hasta
              )
            )
          )

          : hojasActuales;

      if (formato === 'excel') {
        await this.exportService.excel(
          nombre,
          hojas,
          titulo,
          subtitulo
        );
      }
      else await this.exportService.pdf(nombre, titulo, subtitulo, hojas);
    } catch {
      this.alert.error(`No se pudo generar el ${formato === 'excel' ? 'Excel' : 'PDF'}.`);
    } finally {
      this.exportando = false;
    }
  }

  cargarKardex(pagina: number = 0): void {
    if (this.cargandoKardex || pagina < 0) return;

    this.cargandoKardex = true;
    this.errorKardex = '';

    this.movimientoService.listar(pagina, 20).subscribe({
      next: respuesta => {
        this.kardex = respuesta;
        this.cargandoKardex = false;
      },
      error: err => {
        this.cargandoKardex = false;

        if (err.status === 403) {
          this.errorKardex = 'No tienes permisos para consultar el Kardex.';
        } else if (err.status === 401) {
          this.errorKardex = 'Tu sesión ha expirado. Inicia sesión nuevamente.';
        } else {
          this.errorKardex = 'No se pudieron cargar los movimientos.';
        }
      }
    });
  }



  private async obtenerTodosLosMovimientos(): Promise<MovimientoInventario[]> {

    const todos: MovimientoInventario[] = [];
    let pagina = 0;

    while (true) {

      const respuesta = await firstValueFrom(
        this.movimientoService.listar(pagina, 100)
      );

      todos.push(...respuesta.content);

      if (respuesta.last || respuesta.content.length === 0) {
        break;
      }

      pagina++;
    }

    return todos;
  }


  private hojasKardex(movimientos: MovimientoInventario[]): Hoja[] {

    return [{
      nombre: 'Kardex',
      columnas: [
        'N° Movimiento',
        'Fecha',
        'Producto / Lote',
        'Tipo',
        'Cantidad',
        'Stock anterior',
        'Stock nuevo',
        'Referencia',
        'Responsable',
        'Observación'
      ],

      filas: movimientos.map(m => [
        m.idMovimiento,
        this.fechaHora(m.fecha),

        `${m.codigoProducto} - ${m.nombreProducto} / ${m.numeroLote}`,

        m.tipoMovimiento,
        m.cantidad,
        m.stockAnterior,
        m.stockNuevo,

        m.referenciaTipo
          ? `${m.referenciaTipo} #${m.referenciaId ?? '-'}`
          : '-',

        m.nombreUsuario,
        m.observacion || '-'
      ])
    }];
  }


}
