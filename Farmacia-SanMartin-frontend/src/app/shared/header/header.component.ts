import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  

  estaLogueado: boolean = false; 
  nombreUsuario: string = '';
  rolUsuario: string = '';

  constructor(private router: Router) {
  
    this.verificarSesion();
  }

  verificarSesion() {
    const usuarioGuardado = localStorage.getItem('usuario');
    if (usuarioGuardado) {
      const user = JSON.parse(usuarioGuardado);
      this.estaLogueado = true;
      this.nombreUsuario = user.nombre;
      this.rolUsuario = user.rol;
    } else {
      this.estaLogueado = false;
    }
  }

  cerrarSesion() {
    localStorage.removeItem('usuario');
    this.estaLogueado = false;
    this.router.navigate(['/login']);
  }
}