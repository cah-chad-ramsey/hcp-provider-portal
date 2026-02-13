import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Provider,
  ProviderAffiliation,
  ProviderAffiliationRequest,
  VerifyAffiliationRequest,
  PageResponse
} from '../models/provider.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProviderService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  searchProviders(search?: string, page = 0, size = 20): Observable<PageResponse<Provider>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PageResponse<Provider>>(`${this.apiUrl}/providers`, { params });
  }

  getProviderById(id: number): Observable<Provider> {
    return this.http.get<Provider>(`${this.apiUrl}/providers/${id}`);
  }

  requestAffiliation(request: ProviderAffiliationRequest): Observable<ProviderAffiliation> {
    return this.http.post<ProviderAffiliation>(`${this.apiUrl}/providers/associate`, request);
  }

  getUserAffiliations(): Observable<ProviderAffiliation[]> {
    return this.http.get<ProviderAffiliation[]>(`${this.apiUrl}/providers/affiliations`);
  }

  getPendingAffiliations(): Observable<ProviderAffiliation[]> {
    return this.http.get<ProviderAffiliation[]>(`${this.apiUrl}/admin/providers/affiliations`);
  }

  verifyAffiliation(id: number, request: VerifyAffiliationRequest): Observable<ProviderAffiliation> {
    return this.http.post<ProviderAffiliation>(
      `${this.apiUrl}/admin/providers/affiliations/${id}/verify`,
      request
    );
  }
}
