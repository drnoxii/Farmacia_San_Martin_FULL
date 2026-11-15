import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProductoService } from '../../../core/services/producto.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { LaboratorioService } from '../../../core/services/laboratorio.service';
import { AlertService } from '../../../core/services/alert.service';
import { Categoria } from '../../../core/interfaces/categoria.interface';
import { Laboratorio } from '../../../core/interfaces/laboratorio.interface';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FormsModule],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css'
})
export class ProductFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private productoService = inject(ProductoService);
  private categoriaService = inject(CategoriaService);
  private laboratorioService = inject(LaboratorioService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private alertService = inject(AlertService);

  form: FormGroup;
  id: number | null = null;
  get esEdicion(): boolean {
    return this.id !== null;
  }

  categorias: Categoria[] = [];
  laboratorios: Laboratorio[] = [];

  constructor() {
    this.form = this.fb.group({
      codigo: [{ value: 'Automático al guardar', disabled: true }],
      nombreProducto: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: ['', Validators.required],
      presentacion: ['', [
        Validators.required,
        Validators.maxLength(100)
      ]],
      idCategoria: ['', Validators.required],
      idLaboratorio: ['', Validators.required],

      precioVenta: [0, [Validators.required, Validators.min(0.1)]],
      stockMinimo: [5, [Validators.required, Validators.min(0)]],
      estadoGeneral: ['ACTIVO']
    });
  }

  ngOnInit(): void {

    this.categoriaService.listar().subscribe(data => this.categorias = data);
    this.laboratorioService.listar().subscribe(data => this.laboratorios = data);

    // Verificamos si viene un ID en la URL (Modo Editar)
    const idParam = this.route.snapshot.paramMap.get('id');

    this.id = idParam ? Number(idParam) : null;

    if (this.id !== null) {
      this.productoService.buscarPorId(this.id).subscribe({
        next: (p) => this.form.patchValue(p),
        error: () => this.alertService.error('No se pudo cargar el producto')
      });
    }
    if (this.id) {
      this.productoService.buscarPorId(this.id).subscribe({
        next: (p) => this.form.patchValue(p),
        error: () => this.alertService.error('No se pudo cargar el producto')
      });
    }
  }

  guardar() {
    if (this.form.invalid) {
  console.log('Formulario inválido');

  Object.keys(this.form.controls).forEach(campo => {
    const control = this.form.get(campo);

    if (control?.invalid) {
      console.error(campo, control.errors);
    }
  });

  this.form.markAllAsTouched();
  return;
}


    const productoData = {
      ...this.form.value,
      estadoGeneral: this.form.get('estadoGeneral')?.value || 'ACTIVO'
    };

    const request = this.id
      ? this.productoService.actualizar(this.id, productoData)
      : this.productoService.crear(productoData);

    request.subscribe({
      next: () => {
        this.alertService.success(this.id ? 'Producto Actualizado' : 'Producto Creado');
        this.router.navigate(['/productos']);
      },
      error: (err) => {
  console.error('Código HTTP:', err.status);
  console.error('Respuesta del backend:', err.error);

  this.alertService.error(
    err.error?.message || 'No se pudo registrar el producto'
  );
}
    });
  }

  // Variables para los modales
  nuevaCategoria = { nombreCategoria: '', descripCategoria: 'Sin descripción' };
  nuevoLaboratorio = { nombreLaboratorio: '', paisOrigenLab: 'Desconocido' };

  // --- Lógica Categoría ---
  guardarCategoria() {
    if (!this.nuevaCategoria.nombreCategoria) return;

    this.categoriaService.crear(this.nuevaCategoria).subscribe({
      next: (cat) => {
        this.categorias.push(cat);
        this.form.patchValue({ idCategoria: cat.idCategoria }); // Lo autoselecciona
        this.nuevaCategoria = { nombreCategoria: '', descripCategoria: 'Sin descripción' }; // Limpia
        this.alertService.success('Categoría creada');

        // Cierra el modal usando JS puro de Bootstrap
        document.getElementById('btnCerrarModalCat')?.click();
      },
      error: () => this.alertService.error('Error al crear categoría')
    });
  }

  // --- Lógica Laboratorio ---
  guardarLaboratorio() {
    if (!this.nuevoLaboratorio.nombreLaboratorio) return;

    this.laboratorioService.crear(this.nuevoLaboratorio).subscribe({
      next: (lab) => {
        this.laboratorios.push(lab);
        this.form.patchValue({ idLaboratorio: lab.idLaboratorio }); // Lo autoselecciona
        this.nuevoLaboratorio = { nombreLaboratorio: '', paisOrigenLab: 'Desconocido' };
        this.alertService.success('Laboratorio creado');

        document.getElementById('btnCerrarModalLab')?.click();
      },
      error: () => this.alertService.error('Error al crear laboratorio')
    });
  }

}
