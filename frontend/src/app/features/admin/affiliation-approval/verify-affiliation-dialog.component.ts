import { Component, inject, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ProviderAffiliation } from '../../../core/models/provider.model';

@Component({
  selector: 'app-verify-affiliation-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>
      {{ data.approved ? 'Approve' : 'Reject' }} Affiliation Request
    </h2>
    <mat-dialog-content>
      <p><strong>User:</strong> {{ data.affiliation.userName }} ({{ data.affiliation.userEmail }})</p>
      <p><strong>Provider:</strong> {{ data.affiliation.provider.name }}</p>
      <p><strong>Specialty:</strong> {{ data.affiliation.provider.specialty }}</p>
      <p><strong>NPI:</strong> {{ data.affiliation.provider.npi }}</p>

      <form [formGroup]="form">
        <mat-form-field class="full-width" appearance="outline">
          <mat-label>Reason</mat-label>
          <textarea matInput formControlName="reason" rows="4"
                    placeholder="Enter reason for {{ data.approved ? 'approval' : 'rejection' }}"></textarea>
          <mat-error *ngIf="form.get('reason')?.hasError('required')">
            Reason is required
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button
              [color]="data.approved ? 'primary' : 'warn'"
              (click)="onSubmit()"
              [disabled]="form.invalid">
        {{ data.approved ? 'Approve' : 'Reject' }}
      </button>
    </mat-dialog-actions>
  `
})
export class VerifyAffiliationDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<VerifyAffiliationDialogComponent>);

  form: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { affiliation: ProviderAffiliation; approved: boolean }) {
    this.form = this.fb.group({
      reason: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.dialogRef.close({
        approved: this.data.approved,
        reason: this.form.get('reason')?.value
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
