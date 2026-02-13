import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patient, PatientRequest, PageResponse } from '../models/patient.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  createPatient(request: PatientRequest): Observable<Patient> {
    return this.http.post<Patient>(`${this.apiUrl}/patients`, request);
  }

  searchPatients(search?: string, page = 0, size = 20): Observable<PageResponse<Patient>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PageResponse<Patient>>(`${this.apiUrl}/patients`, { params });
  }

  getPatientById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/patients/${id}`);
  }
}
