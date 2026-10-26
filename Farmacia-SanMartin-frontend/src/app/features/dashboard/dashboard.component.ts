import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, RouterModule } from '@angular/router';

interface Modulo {
  titulo: string;
  descripcion: string;
  icono: string;
  ruta: string;
  roles: string[]; 
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  
  rolUsuario: string = '';
  nombreUsuario: string = '';

  // Aquí defines tus opciones grandes
  modulos: Modulo[] = [
    { 
      titulo: 'Punto de Venta', 
      descripcion: 'Generar ventas y emitir boletas/facturas', 
      icono: 'bi-cart-check-fill', 
      ruta: '/ventas', 
      roles: ['ADMIN', 'CAJERO'] 
    },
    { 
      titulo: 'Gestión de Caja', 
      descripcion: 'Apertura, cierre y control de efectivo', 
      icono: 'bi-cash-coin', 
      ruta: '/caja', 
      roles: ['ADMIN', 'CAJERO'] 
    },
    { 
      titulo: 'Productos', 
      descripcion: 'Administrar catálogo de medicamentos', 
      icono: 'bi-capsule-pill', 
      ruta: '/productos', 
      roles: ['ADMIN'] 
    },
    { 
      titulo: 'Inventario y Lotes', 
      descripcion: 'Ver stock, vencimientos y movimientos', 
      icono: 'bi-box-seam-fill', 
      ruta: '/inventario', 
      roles: ['ADMIN'] 
    },
    { 
      titulo: 'Compras y Proveedores', 
      descripcion: 'Registrar ingreso de mercadería', 
      icono: 'bi-truck', 
      ruta: '/compras', 
      roles: ['ADMIN'] 
    },
    { 
      titulo: 'Usuarios', 
      descripcion: 'Gestionar cuentas de Administradores y Cajeros', 
      icono: 'bi-people-fill', 
      ruta: '/usuarios', 
      roles: ['ADMIN'] 
    },
    { 
      titulo: 'Reportes', 
      descripcion: 'Historial de ventas y auditoría', 
      icono: 'bi-graph-up-arrow', 
      ruta: '/reportes', 
      roles: ['ADMIN'] 
    }
  ];

  ngOnInit() {

    const usuarioGuardado = localStorage.getItem('usuario');
    if (usuarioGuardado) {
      const usuario = JSON.parse(usuarioGuardado);
      this.rolUsuario = usuario.rol;
      this.nombreUsuario = usuario.nombre;
    }
  }

  tieneAcceso(rolesPermitidos: string[]): boolean {
    return rolesPermitidos.includes(this.rolUsuario);
  }
}
