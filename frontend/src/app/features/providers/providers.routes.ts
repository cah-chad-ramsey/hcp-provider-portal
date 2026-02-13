import { Routes } from '@angular/router';
import { ProviderAssociationComponent } from './provider-association/provider-association.component';
import { roleGuard } from '../../core/guards/role.guard';

export const PROVIDERS_ROUTES: Routes = [
  {
    path: 'associate',
    component: ProviderAssociationComponent,
    canActivate: [roleGuard(['OFFICE_STAFF', 'SUPPORT_AGENT'])]
  }
];
