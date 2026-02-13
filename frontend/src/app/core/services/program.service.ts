import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Program, SupportService } from '../models/patient.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProgramService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getAllPrograms(): Observable<Program[]> {
    return this.http.get<Program[]>(`${this.apiUrl}/programs`);
  }

  getProgramById(id: number): Observable<Program> {
    return this.http.get<Program>(`${this.apiUrl}/programs/${id}`);
  }

  getProgramServices(programId: number): Observable<SupportService[]> {
    return this.http.get<SupportService[]>(`${this.apiUrl}/programs/${programId}/services`);
  }
}
