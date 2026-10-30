import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LoginComponent } from './features/login/login.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { ProductFormComponent } from './features/products/product-form/product-form.component';

export const routes: Routes = [

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
            
        ]
    },
    {
        path: '**', 
        redirectTo: 'login'
    }
];
