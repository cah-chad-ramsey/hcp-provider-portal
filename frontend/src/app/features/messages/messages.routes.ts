import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';

export const MESSAGES_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./components/thread-list/thread-list.component').then(
            (m) => m.ThreadListComponent
          )
      },
      {
        path: 'compose',
        loadComponent: () =>
          import('./components/compose-message/compose-message.component').then(
            (m) => m.ComposeMessageComponent
          )
      },
      {
        path: 'thread/:id',
        loadComponent: () =>
          import('./components/thread-detail/thread-detail.component').then(
            (m) => m.ThreadDetailComponent
          )
      }
    ]
  }
];
