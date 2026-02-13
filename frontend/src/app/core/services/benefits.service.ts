import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BenefitsInvestigation, BenefitsInvestigationRequest } from '../models/benefits.model';

@Injectable({
  providedIn: 'root'
})
export class BenefitsService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/v1`;

  runInvestigation(patientId: number, request: BenefitsInvestigationRequest): Observable<BenefitsInvestigation> {
    return this.http.post<BenefitsInvestigation>(
      `${this.apiUrl}/patients/${patientId}/benefits-investigation`,
      request
    );
  }

  getPatientInvestigations(patientId: number): Observable<BenefitsInvestigation[]> {
    return this.http.get<BenefitsInvestigation[]>(
      `${this.apiUrl}/patients/${patientId}/benefits-investigation`
    );
  }

  getLatestInvestigation(patientId: number, investigationType: 'MEDICAL' | 'PHARMACY'): Observable<BenefitsInvestigation> {
    const params = new HttpParams().set('investigationType', investigationType);
    return this.http.get<BenefitsInvestigation>(
      `${this.apiUrl}/patients/${patientId}/benefits-investigation/latest`,
      { params }
    );
  }

  getInvestigationById(id: number): Observable<BenefitsInvestigation> {
    return this.http.get<BenefitsInvestigation>(
      `${this.apiUrl}/benefits-investigation/${id}`
    );
  }
}
