import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../core/services/auth.service';
import { ProviderService } from '../../core/services/provider.service';
import { ProviderAffiliation } from '../../core/models/provider.model';
import { NextActionsComponent } from './components/next-actions/next-actions.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatMenuModule,
    MatDividerModule,
    NextActionsComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private providerService = inject(ProviderService);
  private router = inject(Router);

  currentUser$ = this.authService.currentUser$;
  affiliations: ProviderAffiliation[] = [];
  loading = false;

  ngOnInit(): void {
    this.loadAffiliations();
  }

  loadAffiliations(): void {
    this.loading = true;
    this.providerService.getUserAffiliations().subscribe({
      next: (affiliations) => {
        this.affiliations = affiliations;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading affiliations', err);
        this.loading = false;
      }
    });
  }

  get hasApprovedAffiliation(): boolean {
    return this.affiliations.some(a => a.status === 'APPROVED');
  }

  get hasPendingAffiliation(): boolean {
    return this.affiliations.some(a => a.status === 'PENDING');
  }

  onRequestAffiliation(): void {
    this.router.navigate(['/providers/associate']);
  }

  onViewAdmin(): void {
    this.router.navigate(['/admin/affiliations']);
  }

  onViewPatients(): void {
    this.router.navigate(['/patients']);
  }

  onViewForms(): void {
    this.router.navigate(['/forms']);
  }

  onViewMessages(): void {
    this.router.navigate(['/messages']);
  }

  onLogout(): void {
    this.authService.logout();
  }

  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }
}
