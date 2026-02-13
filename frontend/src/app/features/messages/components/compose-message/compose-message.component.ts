import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MessageService } from '../../services/message.service';
import { ProgramService } from '../../../../core/services/program.service';
import { PatientService } from '../../../../core/services/patient.service';
import { Program, Patient } from '../../../../core/models/patient.model';

@Component({
  selector: 'app-compose-message',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './compose-message.component.html',
  styleUrls: ['./compose-message.component.scss']
})
export class ComposeMessageComponent implements OnInit {
  composeForm: FormGroup;
  programs: Program[] = [];
  patients: Patient[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private messageService: MessageService,
    private programService: ProgramService,
    private patientService: PatientService
  ) {
    this.composeForm = this.fb.group({
      subject: ['', [Validators.required, Validators.minLength(3)]],
      programId: [null],
      patientId: [null]
    });
  }

  ngOnInit(): void {
    this.loadPrograms();
    this.loadPatients();
  }

  loadPrograms(): void {
    this.programService.getAllPrograms().subscribe({
      next: (programs) => {
        this.programs = programs;
      },
      error: (error) => {
        console.error('Error loading programs:', error);
      }
    });
  }

  loadPatients(): void {
    this.patientService.searchPatients('', 0, 100).subscribe({
      next: (response) => {
        this.patients = response.content;
      },
      error: (error) => {
        console.error('Error loading patients:', error);
      }
    });
  }

  createThread(): void {
    if (this.composeForm.invalid || this.loading) return;

    this.loading = true;
    this.error = null;

    const request = {
      subject: this.composeForm.value.subject,
      programId: this.composeForm.value.programId || undefined,
      patientId: this.composeForm.value.patientId || undefined
    };

    this.messageService.createThread(request).subscribe({
      next: (thread) => {
        this.router.navigate(['/messages/thread', thread.id]);
      },
      error: (error) => {
        this.error = 'Failed to create message thread';
        this.loading = false;
        console.error('Error creating thread:', error);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/messages']);
  }
}
