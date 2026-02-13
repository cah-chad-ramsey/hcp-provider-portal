import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'providers',
    loadChildren: () => import('./features/providers/providers.routes').then(m => m.PROVIDERS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'patients',
    loadChildren: () => import('./features/patients/patients.routes').then(m => m.PATIENTS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'forms',
    loadChildren: () => import('./features/forms/forms.routes').then(m => m.FORMS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'messages',
    loadChildren: () => import('./features/messages/messages.routes').then(m => m.MESSAGES_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  }
];
