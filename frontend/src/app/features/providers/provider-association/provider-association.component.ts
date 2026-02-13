import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ProviderService } from '../../../core/services/provider.service';
import { Provider } from '../../../core/models/provider.model';

@Component({
  selector: 'app-provider-association',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatPaginatorModule
  ],
  templateUrl: './provider-association.component.html',
  styleUrls: ['./provider-association.component.scss']
})
export class ProviderAssociationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private providerService = inject(ProviderService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  searchForm: FormGroup;
  providers: Provider[] = [];
  loading = false;
  displayedColumns = ['name', 'specialty', 'npi', 'location', 'actions'];

  constructor() {
    this.searchForm = this.fb.group({
      search: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Load initial providers
    this.searchProviders();
  }

  searchProviders(): void {
    this.loading = true;
    const searchTerm = this.searchForm.get('search')?.value || '';

    this.providerService.searchProviders(searchTerm, 0, 20).subscribe({
      next: (response) => {
        this.providers = response.content;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error searching providers', err);
        this.snackBar.open('Error loading providers', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  requestAffiliation(provider: Provider): void {
    this.loading = true;
    this.providerService.requestAffiliation({ providerId: provider.id }).subscribe({
      next: () => {
        this.snackBar.open('Affiliation request submitted successfully', 'Close', { duration: 3000 });
        this.loading = false;
        // Navigate back to dashboard
        setTimeout(() => this.router.navigate(['/dashboard']), 1500);
      },
      error: (err) => {
        console.error('Error requesting affiliation', err);
        this.snackBar.open(err.error?.message || 'Error submitting affiliation request', 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/dashboard']);
  }

  getFullAddress(provider: Provider): string {
    return `${provider.city}, ${provider.state} ${provider.zipCode}`;
  }
}
