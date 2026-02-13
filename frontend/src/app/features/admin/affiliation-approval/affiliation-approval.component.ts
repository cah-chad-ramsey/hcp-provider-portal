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
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { ProviderService } from '../../../core/services/provider.service';
import { ProviderAffiliation } from '../../../core/models/provider.model';
import { VerifyAffiliationDialogComponent } from './verify-affiliation-dialog.component';

@Component({
  selector: 'app-affiliation-approval',
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
    MatDialogModule,
    MatChipsModule
  ],
  templateUrl: './affiliation-approval.component.html',
  styleUrls: ['./affiliation-approval.component.scss']
})
export class AffiliationApprovalComponent implements OnInit {
  private providerService = inject(ProviderService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  affiliations: ProviderAffiliation[] = [];
  loading = false;
  displayedColumns = ['user', 'provider', 'specialty', 'requestedAt', 'actions'];

  ngOnInit(): void {
    this.loadPendingAffiliations();
  }

  loadPendingAffiliations(): void {
    this.loading = true;
    this.providerService.getPendingAffiliations().subscribe({
      next: (affiliations) => {
        this.affiliations = affiliations;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading affiliations', err);
        this.snackBar.open('Error loading affiliation requests', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  openVerifyDialog(affiliation: ProviderAffiliation, approved: boolean): void {
    const dialogRef = this.dialog.open(VerifyAffiliationDialogComponent, {
      width: '500px',
      data: { affiliation, approved }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.verifyAffiliation(affiliation.id, result.approved, result.reason);
      }
    });
  }

  verifyAffiliation(id: number, approved: boolean, reason: string): void {
    this.loading = true;
    this.providerService.verifyAffiliation(id, { approved, reason }).subscribe({
      next: () => {
        this.snackBar.open(
          `Affiliation ${approved ? 'approved' : 'rejected'} successfully`,
          'Close',
          { duration: 3000 }
        );
        this.loadPendingAffiliations();
      },
      error: (err) => {
        console.error('Error verifying affiliation', err);
        this.snackBar.open('Error processing affiliation', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onBack(): void {
    this.router.navigate(['/dashboard']);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
