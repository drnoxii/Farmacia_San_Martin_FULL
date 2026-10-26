import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LoginComponent } from './features/login/login.component';

export const routes: Routes = [

    { 
    path: 'login', 
    component: LoginComponent 
   },

  {
        path: '',
        component: MainLayoutComponent,
        children: [
        
            {path : 'dashboard', component: DashboardComponent}
        ]
    },
    {
        path: '**', 
        redirectTo: 'login'
    }
];
