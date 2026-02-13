import { Routes } from '@angular/router';
import { BenefitsWizardComponent } from './benefits-wizard/benefits-wizard.component';
import { roleGuard } from '../../core/guards/role.guard';

export const BENEFITS_ROUTES: Routes = [
  {
    path: ':id/benefits',
    component: BenefitsWizardComponent,
    canActivate: [roleGuard(['OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN'])]
  }
];
