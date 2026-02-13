import { Routes } from '@angular/router';
import { PatientListComponent } from './patient-list/patient-list.component';
import { PatientCreateComponent } from './patient-create/patient-create.component';
import { PatientDetailComponent } from './patient-detail/patient-detail.component';
import { BenefitsWizardComponent } from '../benefits/benefits-wizard/benefits-wizard.component';
import { roleGuard } from '../../core/guards/role.guard';

export const PATIENTS_ROUTES: Routes = [
  {
    path: '',
    component: PatientListComponent,
    canActivate: [roleGuard(['OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN'])]
  },
  {
    path: 'create',
    component: PatientCreateComponent,
    canActivate: [roleGuard(['OFFICE_STAFF'])]
  },
  {
    path: ':id',
    component: PatientDetailComponent,
    canActivate: [roleGuard(['OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN'])]
  },
  {
    path: ':id/benefits',
    component: BenefitsWizardComponent,
    canActivate: [roleGuard(['OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN'])]
  }
];
