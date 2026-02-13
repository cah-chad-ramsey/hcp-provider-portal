import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Enrollment, EnrollmentRequest } from '../models/patient.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  createOrUpdateEnrollment(patientId: number, request: EnrollmentRequest): Observable<Enrollment> {
    return this.http.post<Enrollment>(
      `${this.apiUrl}/patients/${patientId}/enrollments`,
      request
    );
  }

  getPatientEnrollments(patientId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/patients/${patientId}/enrollments`);
  }

  getEnrollmentById(id: number): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${this.apiUrl}/enrollments/${id}`);
  }

  updateEnrollmentStatus(id: number, status: string, reason?: string): Observable<Enrollment> {
    let params = new HttpParams().set('status', status);
    if (reason) {
      params = params.set('reason', reason);
    }

    return this.http.patch<Enrollment>(
      `${this.apiUrl}/enrollments/${id}/status`,
      null,
      { params }
    );
  }
}
