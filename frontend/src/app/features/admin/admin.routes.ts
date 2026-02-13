import { Routes } from '@angular/router';
import { AffiliationApprovalComponent } from './affiliation-approval/affiliation-approval.component';
import { FormUploadComponent } from '../forms/form-upload/form-upload.component';
import { roleGuard } from '../../core/guards/role.guard';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./components/admin-console/admin-console.component').then(
        (m) => m.AdminConsoleComponent
      ),
    canActivate: [roleGuard(['ADMIN'])]
  },
  {
    path: 'affiliations',
    component: AffiliationApprovalComponent,
    canActivate: [roleGuard(['ADMIN'])]
  },
  {
    path: 'programs',
    loadComponent: () =>
      import('./components/programs-management/programs-management.component').then(
        (m) => m.ProgramsManagementComponent
      ),
    canActivate: [roleGuard(['ADMIN'])]
  },
  {
    path: 'audit',
    loadComponent: () =>
      import('./components/audit-logs/audit-logs.component').then(
        (m) => m.AuditLogsComponent
      ),
    canActivate: [roleGuard(['ADMIN'])]
  },
  {
    path: 'forms/upload',
    component: FormUploadComponent,
    canActivate: [roleGuard(['ADMIN'])]
  }
];
