import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface MessageThread {
  id: number;
  subject: string;
  programId?: number;
  programName?: string;
  patientId?: number;
  patientName?: string;
  createdBy: number;
  createdByName: string;
  createdAt: string;
  lastMessageAt?: string;
  unreadCount: number;
  messages?: Message[];
}

export interface Message {
  id: number;
  threadId: number;
  content: string;
  sentBy: number;
  sentByName: string;
  sentAt: string;
  readAt?: string;
  attachments: MessageAttachment[];
}

export interface MessageAttachment {
  id: number;
  fileName: string;
  fileSize: number;
  mimeType?: string;
  uploadedAt: string;
  downloadUrl: string;
}

export interface CreateThreadRequest {
  subject: string;
  programId?: number;
  patientId?: number;
}

export interface SendMessageRequest {
  content: string;
  attachmentIds?: number[];
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
export class MessageService {
  private apiUrl = `${environment.apiUrl}/messages`;

  constructor(private http: HttpClient) {}

  getThreads(page: number = 0, size: number = 20): Observable<PageResponse<MessageThread>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'lastMessageAt,desc');

    return this.http.get<PageResponse<MessageThread>>(`${this.apiUrl}/threads`, { params });
  }

  getThread(threadId: number): Observable<MessageThread> {
    return this.http.get<MessageThread>(`${this.apiUrl}/threads/${threadId}`);
  }

  createThread(request: CreateThreadRequest): Observable<MessageThread> {
    return this.http.post<MessageThread>(`${this.apiUrl}/threads`, request);
  }

  sendMessage(threadId: number, request: SendMessageRequest): Observable<Message> {
    return this.http.post<Message>(`${this.apiUrl}/threads/${threadId}/messages`, request);
  }

  uploadAttachment(file: File): Observable<MessageAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MessageAttachment>(`${this.apiUrl}/attachments`, formData);
  }

  downloadAttachment(attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/attachments/${attachmentId}/download`, {
      responseType: 'blob'
    });
  }

  getDownloadUrl(attachmentId: number): string {
    return `${this.apiUrl}/attachments/${attachmentId}/download`;
  }
}
