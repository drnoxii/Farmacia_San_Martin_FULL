import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LoginComponent } from './features/login/login.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { ProductFormComponent } from './features/products/product-form/product-form.component';
import { ComprasListComponent } from './features/compras/compras-list/compras-list.component';
import { VentasPosComponent } from './features/ventas-pos/ventas-pos.component';
import { UsuariosComponent } from './features/usuarios/usuarios.component';
import { CajaComponent } from './features/caja/caja.component';
import { ReportesComponent } from './features/reportes/reportes.component';;
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
        canActivateChild: [authGuard],
        children: [
            
            {path : 'dashboard', component: DashboardComponent},
            { path: 'productos', component: ProductListComponent, data: { admin: true }},
            { path: 'productos/nuevo', component: ProductFormComponent, data: { admin: true }},
            { path: 'productos/editar/:id', component: ProductFormComponent, data: { admin: true } },
            {path: 'compras', component: ComprasListComponent, data: { admin: true }},
            {path: 'ventas', component: VentasPosComponent},
            {path: 'usuarios', component: UsuariosComponent, data: { admin: true }},
            { path: 'caja', component: CajaComponent },
            { path: 'reportes', component: ReportesComponent, data: { admin: true } },
        ]
    },
    {
        path: '**', 
        redirectTo: 'dashboard'
    }
];
