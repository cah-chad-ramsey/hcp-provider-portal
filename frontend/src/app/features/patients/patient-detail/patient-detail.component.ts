import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { PatientService } from '../../../core/services/patient.service';
import { EnrollmentService } from '../../../core/services/enrollment.service';
import { BenefitsService } from '../../../core/services/benefits.service';
import { Patient, Enrollment } from '../../../core/models/patient.model';
import { BenefitsInvestigation } from '../../../core/models/benefits.model';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatChipsModule
  ],
  templateUrl: './patient-detail.component.html',
  styleUrls: ['./patient-detail.component.scss']
})
export class PatientDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private patientService = inject(PatientService);
  private enrollmentService = inject(EnrollmentService);
  private benefitsService = inject(BenefitsService);

  patient: Patient | null = null;
  enrollments: Enrollment[] = [];
  investigations: BenefitsInvestigation[] = [];
  loading = true;

  ngOnInit(): void {
    const patientId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPatient(patientId);
    this.loadEnrollments(patientId);
    this.loadInvestigations(patientId);
  }

  loadPatient(id: number): void {
    this.patientService.getPatientById(id).subscribe({
      next: (patient) => {
        this.patient = patient;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading patient', err);
        this.loading = false;
      }
    });
  }

  loadEnrollments(patientId: number): void {
    this.enrollmentService.getPatientEnrollments(patientId).subscribe({
      next: (enrollments) => {
        this.enrollments = enrollments;
      },
      error: (err) => {
        console.error('Error loading enrollments', err);
      }
    });
  }

  loadInvestigations(patientId: number): void {
    this.benefitsService.getPatientInvestigations(patientId).subscribe({
      next: (investigations) => {
        this.investigations = investigations;
      },
      error: (err) => {
        console.error('Error loading investigations', err);
      }
    });
  }

  onBack(): void {
    this.router.navigate(['/patients']);
  }

  enrollPatient(): void {
    if (this.patient) {
      this.router.navigate(['/patients', this.patient.id, 'enroll']);
    }
  }

  runBenefitsInvestigation(): void {
    if (this.patient) {
      this.router.navigate(['/patients', this.patient.id, 'benefits']);
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US');
  }
}
