import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatStepperModule } from '@angular/material/stepper';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { BenefitsService } from '../../../core/services/benefits.service';
import { ProgramService } from '../../../core/services/program.service';
import { PatientService } from '../../../core/services/patient.service';
import { Program } from '../../../core/models/patient.model';
import { Patient } from '../../../core/models/patient.model';
import { BenefitsInvestigation } from '../../../core/models/benefits.model';

@Component({
  selector: 'app-benefits-wizard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatCheckboxModule
  ],
  templateUrl: './benefits-wizard.component.html',
  styleUrls: ['./benefits-wizard.component.scss']
})
export class BenefitsWizardComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private benefitsService = inject(BenefitsService);
  private programService = inject(ProgramService);
  private patientService = inject(PatientService);

  patientId!: number;
  patient: Patient | null = null;
  programs: Program[] = [];
  loading = false;
  investigating = false;
  medicalResult: BenefitsInvestigation | null = null;
  pharmacyResult: BenefitsInvestigation | null = null;

  // Step 1: Enter Information
  informationForm: FormGroup;

  // Step 4: Patient Authorization
  authorizationForm: FormGroup;

  constructor() {
    this.informationForm = this.fb.group({
      programId: ['', Validators.required],
      medicationName: ['', Validators.required],
      payerName: ['', Validators.required],
      payerPlanId: [''],
      memberId: ['', Validators.required],
      patientState: [''],
      patientDob: ['']
    });

    this.authorizationForm = this.fb.group({
      patientConsent: [false, Validators.requiredTrue]
    });
  }

  ngOnInit(): void {
    this.patientId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPatient();
    this.loadPrograms();
  }

  loadPatient(): void {
    this.patientService.getPatientById(this.patientId).subscribe({
      next: (patient) => {
        this.patient = patient;
        // Pre-fill patient state from address
        if (patient.state) {
          this.informationForm.patchValue({ patientState: patient.state });
        }
        if (patient.dateOfBirth) {
          this.informationForm.patchValue({ patientDob: patient.dateOfBirth });
        }
      },
      error: (err) => {
        console.error('Error loading patient', err);
        this.snackBar.open('Error loading patient', 'Close', { duration: 3000 });
      }
    });
  }

  loadPrograms(): void {
    this.programService.getAllPrograms().subscribe({
      next: (programs) => {
        this.programs = programs;
      },
      error: (err) => {
        console.error('Error loading programs', err);
      }
    });
  }

  runInvestigations(): void {
    if (this.informationForm.invalid || this.authorizationForm.invalid) {
      return;
    }

    this.investigating = true;
    const formValue = this.informationForm.value;

    // Run medical investigation
    const medicalRequest = {
      programId: formValue.programId,
      investigationType: 'MEDICAL' as const,
      payerName: formValue.payerName,
      payerPlanId: formValue.payerPlanId,
      memberId: formValue.memberId,
      patientState: formValue.patientState,
      patientDob: formValue.patientDob,
      medicationName: formValue.medicationName
    };

    this.benefitsService.runInvestigation(this.patientId, medicalRequest).subscribe({
      next: (result) => {
        this.medicalResult = result;
        // Run pharmacy investigation after medical completes
        this.runPharmacyInvestigation();
      },
      error: (err) => {
        console.error('Error running medical investigation', err);
        this.snackBar.open('Error running medical investigation', 'Close', { duration: 5000 });
        this.investigating = false;
      }
    });
  }

  runPharmacyInvestigation(): void {
    const formValue = this.informationForm.value;

    const pharmacyRequest = {
      programId: formValue.programId,
      investigationType: 'PHARMACY' as const,
      payerName: formValue.payerName,
      payerPlanId: formValue.payerPlanId,
      memberId: formValue.memberId,
      patientState: formValue.patientState,
      patientDob: formValue.patientDob,
      medicationName: formValue.medicationName
    };

    this.benefitsService.runInvestigation(this.patientId, pharmacyRequest).subscribe({
      next: (result) => {
        this.pharmacyResult = result;
        this.investigating = false;
        this.snackBar.open('Benefits investigation completed successfully', 'Close', { duration: 3000 });
      },
      error: (err) => {
        console.error('Error running pharmacy investigation', err);
        this.snackBar.open('Error running pharmacy investigation', 'Close', { duration: 5000 });
        this.investigating = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/patients', this.patientId]);
  }

  onComplete(): void {
    this.router.navigate(['/patients', this.patientId]);
  }

  getCoverageTypeColor(coverageType: string): string {
    switch (coverageType) {
      case 'MEDICARE': return 'primary';
      case 'MEDICAID': return 'accent';
      case 'COMMERCIAL': return 'primary';
      default: return 'warn';
    }
  }
}
