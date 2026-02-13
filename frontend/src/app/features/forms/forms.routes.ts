import { Routes } from '@angular/router';
import { FormListComponent } from './form-list/form-list.component';
import { FormUploadComponent } from './form-upload/form-upload.component';
import { roleGuard } from '../../core/guards/role.guard';

export const FORMS_ROUTES: Routes = [
  {
    path: '',
    component: FormListComponent,
    canActivate: [roleGuard(['OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN'])]
  }
];

export const ADMIN_FORMS_ROUTES: Routes = [
  {
    path: 'upload',
    component: FormUploadComponent,
    canActivate: [roleGuard(['ADMIN'])]
  }
];
