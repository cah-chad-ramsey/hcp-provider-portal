import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FormResource, FormResourceRequest, FormResourceSearchParams } from '../models/form-resource.model';
import { PageResponse } from '../models/patient.model';

@Injectable({
  providedIn: 'root'
})
export class FormResourceService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/v1`;

  uploadForm(metadata: FormResourceRequest, file: File): Observable<FormResource> {
    const formData = new FormData();
    formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    formData.append('file', file);

    return this.http.post<FormResource>(`${this.apiUrl}/admin/forms`, formData);
  }

  searchForms(params: FormResourceSearchParams): Observable<PageResponse<FormResource>> {
    let httpParams = new HttpParams()
      .set('page', params.page?.toString() || '0')
      .set('size', params.size?.toString() || '20');

    if (params.programId) {
      httpParams = httpParams.set('programId', params.programId.toString());
    }
    if (params.category) {
      httpParams = httpParams.set('category', params.category);
    }
    if (params.searchTerm) {
      httpParams = httpParams.set('searchTerm', params.searchTerm);
    }

    return this.http.get<PageResponse<FormResource>>(`${this.apiUrl}/forms`, { params: httpParams });
  }

  getFormById(id: number): Observable<FormResource> {
    return this.http.get<FormResource>(`${this.apiUrl}/forms/${id}`);
  }

  getFormVersions(id: number): Observable<FormResource[]> {
    return this.http.get<FormResource[]>(`${this.apiUrl}/forms/${id}/versions`);
  }

  deleteForm(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/forms/${id}`);
  }

  getViewUrl(id: number, patientId?: number): string {
    let url = `${this.apiUrl}/forms/${id}/view`;
    if (patientId) {
      url += `?patientId=${patientId}`;
    }
    return url;
  }

  getDownloadUrl(id: number, patientId?: number): string {
    let url = `${this.apiUrl}/forms/${id}/download`;
    if (patientId) {
      url += `?patientId=${patientId}`;
    }
    return url;
  }
}
