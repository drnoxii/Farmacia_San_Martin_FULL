import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  
  loginForm: FormGroup;
  errorMensaje: string = '';
  cargando = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private auth: AuthService
  ) {
    this.loginForm = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onLogin() {
    if (this.cargando) return;
    if (this.loginForm.invalid) {
      this.errorMensaje = 'Por favor, ingresa tu correo y contraseña.';
      return;
    }

   
    this.errorMensaje = '';
    this.cargando = true;
    const { correo, password } = this.loginForm.getRawValue();
    this.auth.login(correo, password).pipe(finalize(() => this.cargando = false)).subscribe({
      next: () => {
        this.loginForm.get('password')?.reset();
        void this.router.navigate(['/dashboard']);
      },
      error: error => {
        this.errorMensaje = error.status === 401
          ? 'Correo o contraseña incorrectos, o cuenta inactiva.'
          : error.status === 0
            ? 'No se pudo conectar con el servidor. Verifica que el backend esté encendido.'
            : 'No se pudo iniciar sesión. Inténtalo nuevamente.';
      }
    });
  }

  onOlvidePassword() {
    this.errorMensaje = 'Contacta al administrador para recuperar el acceso a tu cuenta.';
  }
}
