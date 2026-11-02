
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AlertService } from '../../core/services/alert.service';

interface Usuario {
  idUsuario: number;
  idPersona: number;
  correo: string;
  rol: string;
  estado: string;
  fechaCreacion?: string;
  nombre?: string;
  numeroDocumento?: string;
  telefono?: string;
  direccion?: string;
}

interface RegistroUsuario {
  numeroDocumento: string;
  nombre: string;
  telefono: string;
  direccion: string;
  correo: string;
  password: string;
  rol: string;
}

interface EdicionUsuario {
  idUsuario: number;
  idPersona: number;
  numeroDocumento: string;
  nombre: string;
  telefono: string;
  direccion: string;
  correo: string;
  rol: string;
  estado: string;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {

  private http = inject(HttpClient);
  private alert = inject(AlertService);

  private apiUrl = 'http://localhost:8080/api/v1/usuario';
  private registroUrl = 'http://localhost:8080/api/auth/registro';

  // LISTADO
  usuarios: Usuario[] = [];
  usuariosFiltrados: Usuario[] = [];

  // ESTADOS
  cargando = false;
  guardando = false;
  guardandoEdicion = false;
  mensaje = '';
  error = '';

  // FILTROS
  busqueda = '';
  filtroRol = '';
  filtroEstado = '';

  // REGISTRO
  mostrarFormulario = false;

  nuevoUsuario: RegistroUsuario = this.formularioVacio();

  // EDICIÓN
  mostrarEdicion = false;

  usuarioEdicion: EdicionUsuario = this.edicionVacia();

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  // FORMULARIO VACÍO
  private formularioVacio(): RegistroUsuario {
    return {
      numeroDocumento: '',
      nombre: '',
      telefono: '',
      direccion: '',
      correo: '',
      password: '',
      rol: 'CAJERO'
    };
  }

  // FORMULARIO DE EDICIÓN VACÍO
  private edicionVacia(): EdicionUsuario {
    return {
      idUsuario: 0,
      idPersona: 0,
      numeroDocumento: '',
      nombre: '',
      telefono: '',
      direccion: '',
      correo: '',
      rol: 'CAJERO',
      estado: 'ACTIVO'
    };
  }

  // ABRIR REGISTRO
  abrirFormulario(): void {
    this.nuevoUsuario = this.formularioVacio();
    this.mostrarFormulario = true;
  }

  // CERRAR REGISTRO
  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.nuevoUsuario = this.formularioVacio();
  }

  // REGISTRAR USUARIO
  registrarUsuario(): void {

    if (
      !this.nuevoUsuario.nombre.trim() ||
      !this.nuevoUsuario.numeroDocumento.trim() ||
      !this.nuevoUsuario.correo.trim() ||
      !this.nuevoUsuario.password.trim()
    ) {
      this.alert.error('Completa los campos obligatorios.');
      return;
    }

    if (!/^\d{8}$/.test(this.nuevoUsuario.numeroDocumento)) {
      this.alert.error('El DNI debe contener 8 dígitos.');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.nuevoUsuario.correo)) {
      this.alert.error('Ingresa un correo electrónico válido.');
      return;
    }

    if (this.nuevoUsuario.password.length < 6) {
      this.alert.error('La contraseña debe tener al menos 6 caracteres.');
      return;
    }

    this.guardando = true;

    this.http.post(this.registroUrl, this.nuevoUsuario).subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarFormulario();

        this.alert.success(
          'El usuario se registró correctamente.',
          '¡Usuario registrado!'
        );

        this.cargarUsuarios();
      },
      error: (err) => {
        this.guardando = false;
        console.error('Error al registrar usuario:', err);

        this.alert.error(
          err.error?.message ||
          err.error?.mensaje ||
          'No se pudo registrar el usuario.'
        );
      }
    });
  }

  // LISTAR USUARIOS
  cargarUsuarios(): void {

    this.cargando = true;
    this.error = '';

    this.http.get<Usuario[]>(this.apiUrl).subscribe({
      next: (respuesta) => {
        this.usuarios = respuesta || [];
        this.filtrarUsuarios();
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
        this.cargando = false;
        this.error = 'No se pudieron cargar los usuarios.';
        this.alert.error(this.error);
      }
    });
  }

  // BUSCAR Y FILTRAR
  filtrarUsuarios(): void {

    const texto = this.busqueda.toLowerCase().trim();

    this.usuariosFiltrados = this.usuarios.filter(usuario => {

      const coincideTexto =
        (usuario.correo || '').toLowerCase().includes(texto) ||
        (usuario.nombre || '').toLowerCase().includes(texto) ||
        (usuario.numeroDocumento || '').includes(texto) ||
        (usuario.telefono || '').includes(texto) ||
        String(usuario.idUsuario).includes(texto);

      const coincideRol =
        !this.filtroRol || usuario.rol === this.filtroRol;

      const coincideEstado =
        !this.filtroEstado || usuario.estado === this.filtroEstado;

      return coincideTexto && coincideRol && coincideEstado;
    });
  }

  // LIMPIAR FILTROS
  limpiarFiltros(): void {
    this.busqueda = '';
    this.filtroRol = '';
    this.filtroEstado = '';
    this.filtrarUsuarios();
  }

  // CONTADORES
  get totalUsuarios(): number {
    return this.usuarios.length;
  }

  get totalActivos(): number {
    return this.usuarios.filter(
      usuario => usuario.estado === 'ACTIVO'
    ).length;
  }

  get totalAdministradores(): number {
    return this.usuarios.filter(
      usuario => usuario.rol === 'ADMIN'
    ).length;
  }

  get totalCajeros(): number {
    return this.usuarios.filter(
      usuario => usuario.rol === 'CAJERO'
    ).length;
  }

  // ABRIR EDICIÓN
  editarUsuario(usuario: Usuario): void {

    this.usuarioEdicion = {
      idUsuario: usuario.idUsuario,
      idPersona: usuario.idPersona,
      numeroDocumento: usuario.numeroDocumento || '',
      nombre: usuario.nombre || '',
      telefono: usuario.telefono || '',
      direccion: usuario.direccion || '',
      correo: usuario.correo || '',
      rol: usuario.rol,
      estado: usuario.estado
    };

    this.mostrarEdicion = true;
  }

  // CERRAR EDICIÓN
  cerrarEdicion(): void {
    this.mostrarEdicion = false;
    this.usuarioEdicion = this.edicionVacia();
  }

  // GUARDAR EDICIÓN
  guardarCambios(): void {

    if (
      !this.usuarioEdicion.nombre.trim() ||
      !this.usuarioEdicion.numeroDocumento.trim() ||
      !this.usuarioEdicion.correo.trim()
    ) {
      this.alert.error('Completa los campos obligatorios.');
      return;
    }

    if (!/^\d{8}$/.test(this.usuarioEdicion.numeroDocumento)) {
      this.alert.error('El DNI debe contener 8 dígitos.');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.usuarioEdicion.correo)) {
      this.alert.error('Ingresa un correo electrónico válido.');
      return;
    }

    if (!this.usuarioEdicion.idUsuario) {
      this.alert.error('No se encontró el ID del usuario.');
      return;
    }

    this.guardandoEdicion = true;

    this.http.put<Usuario>(
      `${this.apiUrl}/${this.usuarioEdicion.idUsuario}`,
      this.usuarioEdicion
    ).subscribe({
      next: () => {
        this.guardandoEdicion = false;
        this.cerrarEdicion();

        this.alert.success(
          'Los datos del usuario se actualizaron correctamente.',
          '¡Cambios guardados!'
        );

        this.cargarUsuarios();
      },
      error: (err) => {
        this.guardandoEdicion = false;
        console.error('Error al actualizar usuario:', err);

        this.alert.error(
          err.error?.message ||
          err.error?.mensaje ||
          'No se pudieron guardar los cambios.'
        );
      }
    });
  }

  // ACTIVAR O DESACTIVAR
  async cambiarEstado(usuario: Usuario): Promise<void> {

    const nuevoEstado =
      usuario.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';

    const confirmado = await this.alert.confirm(
      `¿Deseas ${nuevoEstado === 'ACTIVO' ? 'activar' : 'desactivar'} a ${usuario.nombre || usuario.correo}?`,
      'Confirmar cambio de estado'
    );

    if (!confirmado) return;

    this.http.put<Usuario>(
      `${this.apiUrl}/${usuario.idUsuario}/estado?estado=${nuevoEstado}`,
      null
    ).subscribe({
      next: () => {
        this.alert.success(
          `El usuario ahora se encuentra ${nuevoEstado}.`,
          'Estado actualizado'
        );

        this.cargarUsuarios();
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
        this.alert.error('No se pudo actualizar el estado del usuario.');
      }
    });
  }

  // ELIMINAR USUARIO
  async eliminarUsuario(usuario: Usuario): Promise<void> {

    const confirmado = await this.alert.confirm(
      `¿Estás seguro de eliminar la cuenta de ${usuario.nombre || usuario.correo}? Esta acción no se puede deshacer.`,
      'Eliminar usuario'
    );

    if (!confirmado) return;

    this.http.delete<void>(
      `${this.apiUrl}/${usuario.idUsuario}`
    ).subscribe({
      next: () => {
        this.alert.success(
          'El usuario ha sido eliminado correctamente.',
          'Usuario eliminado'
        );

        this.cargarUsuarios();
      },
      error: (err) => {
        console.error('Error al eliminar usuario:', err);
        this.alert.error('No se pudo eliminar el usuario.');
      }
    });
  }

  // LIMPIAR MENSAJES
  limpiarMensajes(): void {
    this.mensaje = '';
    this.error = '';
  }
}
