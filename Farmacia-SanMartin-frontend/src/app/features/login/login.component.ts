import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

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

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      correo: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onLogin() {
    if (this.loginForm.invalid) {
      this.errorMensaje = 'Por favor, ingresa tu correo y contraseña.';
      return;
    }

   
    const usuarioFalso = {
      idUsuario: 1,
      nombre: 'Admin Prueba',
      rol: 'ADMIN'
    };
    
  
    localStorage.setItem('usuario', JSON.stringify(usuarioFalso));
    
  
    this.router.navigate(['/dashboard']);
  }

  onOlvidePassword() {
    alert('Simulación: Se envió un correo de recuperación.');
  }
}