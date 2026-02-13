import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PatientService } from '../../../core/services/patient.service';

@Component({
  selector: 'app-patient-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './patient-create.component.html',
  styleUrls: ['./patient-create.component.scss']
})
export class PatientCreateComponent {
  private fb = inject(FormBuilder);
  private patientService = inject(PatientService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  patientForm: FormGroup;
  loading = false;
  maxDate = new Date();

  genderOptions = [
    { value: 'MALE', label: 'Male' },
    { value: 'FEMALE', label: 'Female' },
    { value: 'OTHER', label: 'Other' },
    { value: 'PREFER_NOT_TO_SAY', label: 'Prefer not to say' }
  ];

  constructor() {
    this.patientForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      dateOfBirth: ['', Validators.required],
      gender: [''],
      phone: [''],
      email: ['', Validators.email],
      addressLine1: [''],
      addressLine2: [''],
      city: [''],
      state: [''],
      zipCode: ['']
    });
  }

  onSubmit(): void {
    if (this.patientForm.invalid) {
      return;
    }

    this.loading = true;
    const formValue = this.patientForm.value;

    // Format date to YYYY-MM-DD
    const dob = new Date(formValue.dateOfBirth);
    const formattedDob = dob.toISOString().split('T')[0];

    const request = {
      ...formValue,
      dateOfBirth: formattedDob
    };

    this.patientService.createPatient(request).subscribe({
      next: (patient) => {
        this.snackBar.open('Patient created successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/patients', patient.id]);
      },
      error: (err) => {
        console.error('Error creating patient', err);
        const message = err.error?.message || 'Error creating patient';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/patients']);
  }
}
