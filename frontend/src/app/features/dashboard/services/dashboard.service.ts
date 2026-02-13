import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface NextAction {
  id: string;
  title: string;
  description: string;
  actionType: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  resourceId?: number;
  resourceName?: string;
  actionUrl: string;
  icon: string;
  daysOverdue?: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getNextActions(): Observable<NextAction[]> {
    return this.http.get<NextAction[]>(`${this.apiUrl}/next-actions`);
  }
}
