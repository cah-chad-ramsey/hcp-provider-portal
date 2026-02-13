import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AddServiceRequest {
  name: string;
  description?: string;
  serviceType: string;
}

export interface SupportService {
  id: number;
  programId: number;
  name: string;
  description?: string;
  serviceType: string;
  active: boolean;
}

export interface AuditEvent {
  id: number;
  eventType: string;
  userId?: number;
  userName: string;
  userEmail?: string;
  resourceType: string;
  resourceId?: number;
  action: string;
  correlationId: string;
  ipAddress: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  addServiceToProgram(programId: number, request: AddServiceRequest): Observable<SupportService> {
    return this.http.post<SupportService>(`${this.apiUrl}/programs/${programId}/services`, request);
  }

  getAuditLogs(
    page: number = 0,
    size: number = 50,
    filters?: {
      eventType?: string;
      userId?: number;
      action?: string;
      correlationId?: string;
      startDate?: string;
      endDate?: string;
    }
  ): Observable<PageResponse<AuditEvent>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');

    if (filters) {
      if (filters.eventType) params = params.set('eventType', filters.eventType);
      if (filters.userId) params = params.set('userId', filters.userId.toString());
      if (filters.action) params = params.set('action', filters.action);
      if (filters.correlationId) params = params.set('correlationId', filters.correlationId);
      if (filters.startDate) params = params.set('startDate', filters.startDate);
      if (filters.endDate) params = params.set('endDate', filters.endDate);
    }

    return this.http.get<PageResponse<AuditEvent>>(`${this.apiUrl}/audit`, { params });
  }
}
