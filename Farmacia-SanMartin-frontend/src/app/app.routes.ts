import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LoginComponent } from './features/login/login.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { ProductFormComponent } from './features/products/product-form/product-form.component';
import { ComprasListComponent } from './features/compras/compras-list/compras-list.component';
import { VentasPosComponent } from './features/ventas-pos/ventas-pos.component';
import { UsuariosComponent } from './features/usuarios/usuarios.component';
import { CajaComponent } from './features/caja/caja.component';

export const routes: Routes = [

    {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
    { 
    path: 'login', 
    component: LoginComponent 
   },

  {
        path: '',
        component: MainLayoutComponent,
        children: [
            
            {path : 'dashboard', component: DashboardComponent},
            { path: 'productos', component: ProductListComponent},
            { path: 'productos/nuevo', component: ProductFormComponent},
            { path: 'productos/editar/:id', component: ProductFormComponent },
            {path: 'compras', component: ComprasListComponent},
            {path: 'ventas', component: VentasPosComponent},
            {path: 'usuarios', component: UsuariosComponent},
            { path: 'caja', component: CajaComponent },
        ]
    },
    {
        path: '**', 
        redirectTo: 'dashboard'
    }
];
