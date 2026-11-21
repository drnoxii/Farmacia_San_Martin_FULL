import { Component, OnInit, OnDestroy, HostListener, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { Subscription, filter } from 'rxjs';
import { ReporteService } from '../../core/services/reporte.service';
import { AuthService } from '../../core/services/auth.service';
import { ReporteInventario } from '../../core/interfaces/reporte.interface';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {
  

  estaLogueado: boolean = false; 
  nombreUsuario: string = '';
  rolUsuario: string = '';

  notificacionesAbiertas = false;
  cargandoNotificaciones = false;
  errorNotificaciones = '';
  inventarioAlertas: ReporteInventario | null = null;
  readonly diasAviso = 30;
  private navegacion?: Subscription;
  private consulta?: Subscription;
  @ViewChild('panelNotificaciones') panelNotificaciones?: ElementRef<HTMLElement>;
  @ViewChild('botonNotificaciones') botonNotificaciones?: ElementRef<HTMLButtonElement>;

  constructor(private router: Router, private reportes: ReporteService, private auth: AuthService) {
  
    this.verificarSesion();
  }

  get esAdministrador(): boolean {
    return this.estaLogueado && this.rolUsuario === 'ADMIN';
  }

  get totalNotificaciones(): number {
    return (this.inventarioAlertas?.stockBajo.length ?? 0)
      + (this.inventarioAlertas?.lotesPorVencer.length ?? 0)
      + (this.inventarioAlertas?.lotesVencidos.length ?? 0);
  }

  ngOnInit(): void {
    this.cargarNotificaciones();
    this.navegacion = this.router.events.pipe(filter(evento => evento instanceof NavigationEnd))
      .subscribe(() => {
        this.notificacionesAbiertas = false;
        this.verificarSesion();
        this.cargarNotificaciones();
      });
  }

  ngOnDestroy(): void {
    this.navegacion?.unsubscribe();
    this.consulta?.unsubscribe();
  }

  alternarNotificaciones(): void {
    this.verificarSesion();
    if (!this.esAdministrador) return;
    this.notificacionesAbiertas = !this.notificacionesAbiertas;
    if (this.notificacionesAbiertas) this.cargarNotificaciones();
  }

  abrirInventario(seccion: 'stock-bajo' | 'por-vencer' | 'vencidos'): void {
    this.notificacionesAbiertas = false;
    void this.router.navigate(['/reportes'], { queryParams: { tab: 'inventario', seccion } })
      .then(() => document.getElementById(seccion)?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
  }

  cargarNotificaciones(): void {
    this.consulta?.unsubscribe();
    if (!this.esAdministrador) {
      this.limpiarNotificaciones();
      return;
    }
    this.cargandoNotificaciones = true;
    this.errorNotificaciones = '';
    this.consulta = this.reportes.inventario(this.diasAviso).subscribe({
      next: datos => {
        this.inventarioAlertas = datos;
        this.cargandoNotificaciones = false;
      },
      error: () => {
        this.inventarioAlertas = null;
        this.cargandoNotificaciones = false;
        this.errorNotificaciones = 'No se pudieron cargar las notificaciones.';
      }
    });
  }

  private limpiarNotificaciones(): void {
    this.consulta?.unsubscribe();
    this.notificacionesAbiertas = false;
    this.inventarioAlertas = null;
    this.errorNotificaciones = '';
    this.cargandoNotificaciones = false;
  }

  @HostListener('document:click', ['$event'])
  cerrarAlPulsarFuera(evento: MouseEvent): void {
    if (this.notificacionesAbiertas && !this.panelNotificaciones?.nativeElement.contains(evento.target as Node)) {
      this.notificacionesAbiertas = false;
    }
  }

  @HostListener('document:keydown.escape')
  cerrarConEscape(): void {
    if (this.notificacionesAbiertas) {
      this.notificacionesAbiertas = false;
      this.botonNotificaciones?.nativeElement.focus();
    }
  }

  verificarSesion() {
    this.estaLogueado = false;
    this.nombreUsuario = '';
    this.rolUsuario = '';
    try {
      const user = this.auth.getUsuario();
      if (user) {
        this.estaLogueado = !!user;
        this.nombreUsuario = user?.nombre ?? '';
        this.rolUsuario = user?.rol ?? '';
      }
    } catch { /* Una sesión inválida se trata como sesión cerrada. */ }
    if (!this.esAdministrador) this.limpiarNotificaciones();
  }

  cerrarSesion() {
    this.estaLogueado = false;
    this.limpiarNotificaciones();
    this.auth.cerrarSesion();
  }
}
