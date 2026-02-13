import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormResourceService } from '../../../core/services/form-resource.service';
import { ProgramService } from '../../../core/services/program.service';
import { Program } from '../../../core/models/patient.model';

@Component({
  selector: 'app-form-upload',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './form-upload.component.html',
  styleUrls: ['./form-upload.component.scss']
})
export class FormUploadComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private formResourceService = inject(FormResourceService);
  private programService = inject(ProgramService);

  uploadForm: FormGroup;
  programs: Program[] = [];
  loading = false;
  selectedFile: File | null = null;
  filePreview: string | null = null;

  categories = ['Enrollment', 'Consent', 'Medical', 'Insurance', 'Financial', 'Educational', 'Other'];

  constructor() {
    this.uploadForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      programId: [''],
      category: ['', Validators.required],
      complianceApproved: [false]
    });
  }

  ngOnInit(): void {
    this.loadPrograms();
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];

      // Generate preview for images
      if (this.selectedFile.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.filePreview = e.target?.result as string;
        };
        reader.readAsDataURL(this.selectedFile);
      } else {
        this.filePreview = null;
      }
    }
  }

  onSubmit(): void {
    if (this.uploadForm.invalid || !this.selectedFile) {
      return;
    }

    this.loading = true;
    const formValue = this.uploadForm.value;

    // Prepare metadata
    const metadata = {
      title: formValue.title,
      description: formValue.description,
      programId: formValue.programId || null,
      category: formValue.category,
      complianceApproved: formValue.complianceApproved
    };

    this.formResourceService.uploadForm(metadata, this.selectedFile).subscribe({
      next: (response) => {
        this.snackBar.open('Form uploaded successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/forms']);
      },
      error: (err) => {
        console.error('Error uploading form', err);
        const message = err.error?.message || 'Error uploading form';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/forms']);
  }

  getFileIcon(): string {
    if (!this.selectedFile) return 'insert_drive_file';

    const type = this.selectedFile.type;
    if (type.includes('pdf')) return 'picture_as_pdf';
    if (type.includes('word')) return 'description';
    if (type.includes('excel') || type.includes('spreadsheet')) return 'table_chart';
    if (type.includes('image')) return 'image';
    return 'insert_drive_file';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }
}
