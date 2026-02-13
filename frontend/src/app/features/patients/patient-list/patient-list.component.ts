import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { PatientService } from '../../../core/services/patient.service';
import { Patient } from '../../../core/models/patient.model';

@Component({
  selector: 'app-patient-list',
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
    MatPaginatorModule
  ],
  templateUrl: './patient-list.component.html',
  styleUrls: ['./patient-list.component.scss']
})
export class PatientListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private patientService = inject(PatientService);
  private router = inject(Router);

  searchForm: FormGroup;
  patients: Patient[] = [];
  loading = false;
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  displayedColumns = ['referenceId', 'name', 'dateOfBirth', 'contact', 'actions'];

  constructor() {
    this.searchForm = this.fb.group({
      search: ['']
    });
  }

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.loading = true;
    const searchTerm = this.searchForm.get('search')?.value || '';

    this.patientService.searchPatients(searchTerm, this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.patients = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading patients', err);
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.pageIndex = 0;
    this.loadPatients();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPatients();
  }

  viewPatient(patient: Patient): void {
    this.router.navigate(['/patients', patient.id]);
  }

  createPatient(): void {
    this.router.navigate(['/patients/create']);
  }

  getPatientName(patient: Patient): string {
    return `${patient.firstName} ${patient.lastName}`;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US');
  }
}
