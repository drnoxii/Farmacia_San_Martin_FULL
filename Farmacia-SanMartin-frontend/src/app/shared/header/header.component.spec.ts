import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HeaderComponent } from './header.component';
import { ReporteService } from '../../core/services/reporte.service';
import { ReporteInventario } from '../../core/interfaces/reporte.interface';

describe('Notificaciones de inventario del administrador', () => {
  let reportes: jasmine.SpyObj<ReporteService>;
  let sesion: string | null;
  const inventario: ReporteInventario = {
    fecha: '2026-11-14', dias: 30, lotesVencidos: [],
    stockBajo: [{ codigo: 'IBU-400', producto: 'Ibuprofeno', categoria: 'Medicamento', stockActual: 2, stockMinimo: 10, faltante: 8 }],
    lotesPorVencer: [{ codigo: 'IBU-400', producto: 'Ibuprofeno', numeroLote: 'L-01', fechaVencimiento: '2026-11-20', stock: 2, diasRestantes: 6 }]
  };

  beforeEach(async () => {
    sesion = JSON.stringify({ nombre: 'Administrador', rol: 'ADMIN' });
    spyOn(Storage.prototype, 'getItem').and.callFake(key => key === 'usuario' ? sesion : null);
    reportes = jasmine.createSpyObj('ReporteService', ['inventario']);
    reportes.inventario.and.returnValue(of(inventario));
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [provideRouter([]), { provide: ReporteService, useValue: reportes }]
    }).compileComponents();
  });

  it('muestra la campana y abre las dos categorías al pulsarla', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    expect(reportes.inventario).toHaveBeenCalledWith(30);
    expect(fixture.componentInstance.totalNotificaciones).toBe(2);
    expect(fixture.nativeElement.querySelector('#alertas-inventario')).toBeNull();
    fixture.nativeElement.querySelector('.boton-notificaciones').click();
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('#alertas-inventario');
    expect(panel.textContent).toContain('Stock bajo');
    expect(panel.textContent).toContain('Por vencer');
    expect(panel.textContent).toContain('L-01');
    fixture.componentInstance.cerrarConEscape();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('#alertas-inventario')).toBeNull();
  });

  it('no muestra ni consulta alertas para un cajero', () => {
    sesion = JSON.stringify({ nombre: 'Cajero', rol: 'CAJERO' });
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.boton-notificaciones')).toBeNull();
    expect(reportes.inventario).not.toHaveBeenCalled();
  });

  it('oculta y limpia las alertas cuando deja de ser administrador', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    fixture.componentInstance.alternarNotificaciones();
    sesion = null;
    fixture.componentInstance.verificarSesion();
    fixture.detectChanges();
    expect(fixture.componentInstance.inventarioAlertas).toBeNull();
    expect(fixture.nativeElement.querySelector('.notificaciones')).toBeNull();
  });

  it('distingue un inventario sin alertas de un error de consulta', () => {
    reportes.inventario.and.returnValue(of({ ...inventario, stockBajo: [], lotesPorVencer: [] }));
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    fixture.componentInstance.alternarNotificaciones();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No hay productos con stock bajo');
    reportes.inventario.and.returnValue(throwError(() => new Error('Servidor no disponible')));
    fixture.componentInstance.cargarNotificaciones();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
    expect(fixture.nativeElement.textContent).not.toContain('No hay productos con stock bajo');
  });
});
