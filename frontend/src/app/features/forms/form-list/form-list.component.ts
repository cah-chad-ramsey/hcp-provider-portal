import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormResourceService } from '../../../core/services/form-resource.service';
import { ProgramService } from '../../../core/services/program.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormResource } from '../../../core/models/form-resource.model';
import { Program } from '../../../core/models/patient.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-form-list',
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
    MatChipsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './form-list.component.html',
  styleUrls: ['./form-list.component.scss']
})
export class FormListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private formResourceService = inject(FormResourceService);
  private programService = inject(ProgramService);
  private authService = inject(AuthService);

  forms: FormResource[] = [];
  programs: Program[] = [];
  loading = false;

  // Pagination
  totalElements = 0;
  pageSize = 12;
  pageIndex = 0;

  // Filters
  filterForm: FormGroup;

  categories = ['Enrollment', 'Consent', 'Medical', 'Insurance', 'Financial', 'Educational', 'Other'];

  constructor() {
    this.filterForm = this.fb.group({
      programId: [''],
      category: [''],
      searchTerm: ['']
    });
  }

  ngOnInit(): void {
    this.loadPrograms();
    this.loadForms();
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

  loadForms(): void {
    this.loading = true;
    const filters = this.filterForm.value;

    this.formResourceService.searchForms({
      programId: filters.programId || undefined,
      category: filters.category || undefined,
      searchTerm: filters.searchTerm || undefined,
      page: this.pageIndex,
      size: this.pageSize
    }).subscribe({
      next: (response) => {
        this.forms = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading forms', err);
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.pageIndex = 0;
    this.loadForms();
  }

  onClearFilters(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.loadForms();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadForms();
  }

  onUploadForm(): void {
    this.router.navigate(['/admin/forms/upload']);
  }

  onViewForm(form: FormResource): void {
    const url = this.formResourceService.getViewUrl(form.id);
    window.open(url, '_blank');
  }

  onDownloadForm(form: FormResource): void {
    const url = this.formResourceService.getDownloadUrl(form.id);
    window.open(url, '_blank');
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  getFileIcon(mimeType: string): string {
    if (mimeType.includes('pdf')) return 'picture_as_pdf';
    if (mimeType.includes('word')) return 'description';
    if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) return 'table_chart';
    if (mimeType.includes('image')) return 'image';
    return 'insert_drive_file';
  }

  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }
}
