import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService, AuditEvent } from '../../services/admin.service';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './audit-logs.component.html',
  styleUrls: ['./audit-logs.component.scss']
})
export class AuditLogsComponent implements OnInit {
  filterForm: FormGroup;
  auditLogs: AuditEvent[] = [];
  loading = false;
  error: string | null = null;

  displayedColumns = ['createdAt', 'eventType', 'userName', 'resourceType', 'action', 'ipAddress'];

  // Pagination
  totalElements = 0;
  pageSize = 50;
  pageIndex = 0;

  eventTypes = [
    'PATIENT_CREATED',
    'ENROLLMENT_CREATED',
    'ENROLLMENT_UPDATED',
    'BENEFITS_INVESTIGATION_RUN',
    'FORM_UPLOADED',
    'FORM_DOWNLOADED',
    'MESSAGE_THREAD_CREATED',
    'MESSAGE_SENT',
    'ATTACHMENT_UPLOADED',
    'ATTACHMENT_DOWNLOADED'
  ];

  actions = ['CREATE', 'VIEW', 'UPDATE', 'DELETE'];

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      eventType: [''],
      action: [''],
      correlationId: [''],
      startDate: [null],
      endDate: [null]
    });
  }

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.loading = true;
    this.error = null;

    const filters = {
      eventType: this.filterForm.value.eventType || undefined,
      action: this.filterForm.value.action || undefined,
      correlationId: this.filterForm.value.correlationId || undefined,
      startDate: this.filterForm.value.startDate ?
        new Date(this.filterForm.value.startDate).toISOString() : undefined,
      endDate: this.filterForm.value.endDate ?
        new Date(this.filterForm.value.endDate).toISOString() : undefined
    };

    this.adminService.getAuditLogs(this.pageIndex, this.pageSize, filters).subscribe({
      next: (response) => {
        this.auditLogs = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load audit logs';
        this.loading = false;
        console.error('Error loading audit logs:', error);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAuditLogs();
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadAuditLogs();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.loadAuditLogs();
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
