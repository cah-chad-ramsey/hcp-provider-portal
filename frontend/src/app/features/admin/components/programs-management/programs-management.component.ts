import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProgramService } from '../../../../core/services/program.service';
import { AdminService } from '../../services/admin.service';
import { Program, SupportService } from '../../../../core/models/patient.model';

@Component({
  selector: 'app-programs-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './programs-management.component.html',
  styleUrls: ['./programs-management.component.scss']
})
export class ProgramsManagementComponent implements OnInit {
  programs: Program[] = [];
  programServices: Map<number, SupportService[]> = new Map();
  loading = false;
  error: string | null = null;

  addServiceForm: FormGroup;
  selectedProgramId: number | null = null;
  addingService = false;

  serviceTypes = [
    'FINANCIAL_ASSISTANCE',
    'COPAY_ASSISTANCE',
    'PATIENT_EDUCATION',
    'ADHERENCE_SUPPORT',
    'CARE_COORDINATION',
    'SPECIALTY_PHARMACY',
    'PRIOR_AUTHORIZATION',
    'OTHER'
  ];

  constructor(
    private programService: ProgramService,
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.addServiceForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', Validators.maxLength(1000)],
      serviceType: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadPrograms();
  }

  loadPrograms(): void {
    this.loading = true;
    this.error = null;

    this.programService.getAllPrograms().subscribe({
      next: (programs) => {
        this.programs = programs;
        // Load services for each program
        programs.forEach(program => {
          this.loadProgramServices(program.id);
        });
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load programs';
        this.loading = false;
        console.error('Error loading programs:', error);
      }
    });
  }

  loadProgramServices(programId: number): void {
    this.programService.getProgramServices(programId).subscribe({
      next: (services) => {
        this.programServices.set(programId, services);
      },
      error: (error) => {
        console.error(`Error loading services for program ${programId}:`, error);
      }
    });
  }

  getProgramServices(programId: number): SupportService[] {
    return this.programServices.get(programId) || [];
  }

  openAddServiceDialog(programId: number): void {
    this.selectedProgramId = programId;
    this.addServiceForm.reset();
  }

  cancelAddService(): void {
    this.selectedProgramId = null;
    this.addServiceForm.reset();
  }

  addService(): void {
    if (this.addServiceForm.invalid || !this.selectedProgramId || this.addingService) return;

    this.addingService = true;
    this.error = null;

    const request = {
      name: this.addServiceForm.value.name,
      description: this.addServiceForm.value.description || undefined,
      serviceType: this.addServiceForm.value.serviceType
    };

    this.adminService.addServiceToProgram(this.selectedProgramId, request).subscribe({
      next: (service) => {
        // Add the new service to the map
        const services = this.programServices.get(this.selectedProgramId!) || [];
        services.push(service);
        this.programServices.set(this.selectedProgramId!, services);

        this.addingService = false;
        this.cancelAddService();
      },
      error: (error) => {
        this.error = 'Failed to add service';
        this.addingService = false;
        console.error('Error adding service:', error);
      }
    });
  }

  formatServiceType(serviceType: string): string {
    return serviceType.replace(/_/g, ' ');
  }
}
