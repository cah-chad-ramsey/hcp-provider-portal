import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export function roleGuard(allowedRoles: string[]): CanActivateFn {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasAnyRole(allowedRoles)) {
      return true;
    }

    // Redirect to dashboard if user doesn't have required role
    router.navigate(['/dashboard']);
    return false;
  };
}
