import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MessageService, MessageThread, Message, MessageAttachment } from '../../services/message.service';

@Component({
  selector: 'app-thread-detail',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatChipsModule
  ],
  templateUrl: './thread-detail.component.html',
  styleUrls: ['./thread-detail.component.scss']
})
export class ThreadDetailComponent implements OnInit {
  thread: MessageThread | null = null;
  loading = false;
  sending = false;
  error: string | null = null;

  replyForm: FormGroup;
  selectedFiles: File[] = [];
  uploadedAttachments: MessageAttachment[] = [];
  uploadingFiles = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
    private fb: FormBuilder
  ) {
    this.replyForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(1)]]
    });
  }

  ngOnInit(): void {
    const threadId = this.route.snapshot.paramMap.get('id');
    if (threadId) {
      this.loadThread(+threadId);
    }
  }

  loadThread(threadId: number): void {
    this.loading = true;
    this.error = null;

    this.messageService.getThread(threadId).subscribe({
      next: (thread) => {
        this.thread = thread;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load thread';
        this.loading = false;
        console.error('Error loading thread:', error);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
      this.uploadFiles();
    }
  }

  uploadFiles(): void {
    if (this.selectedFiles.length === 0) return;

    this.uploadingFiles = true;
    const uploads = this.selectedFiles.map(file =>
      this.messageService.uploadAttachment(file).toPromise()
    );

    Promise.all(uploads)
      .then((attachments) => {
        this.uploadedAttachments.push(...attachments.filter(a => a !== undefined) as MessageAttachment[]);
        this.uploadingFiles = false;
        this.selectedFiles = [];
      })
      .catch((error) => {
        console.error('Error uploading files:', error);
        this.error = 'Failed to upload files';
        this.uploadingFiles = false;
      });
  }

  removeAttachment(index: number): void {
    this.uploadedAttachments.splice(index, 1);
  }

  sendReply(): void {
    if (this.replyForm.invalid || !this.thread || this.sending) return;

    this.sending = true;
    this.error = null;

    const request = {
      content: this.replyForm.value.content,
      attachmentIds: this.uploadedAttachments.map(a => a.id)
    };

    this.messageService.sendMessage(this.thread.id, request).subscribe({
      next: (message) => {
        // Add new message to thread
        if (this.thread) {
          if (!this.thread.messages) {
            this.thread.messages = [];
          }
          this.thread.messages.push(message);
        }

        // Reset form
        this.replyForm.reset();
        this.uploadedAttachments = [];
        this.sending = false;
      },
      error: (error) => {
        this.error = 'Failed to send message';
        this.sending = false;
        console.error('Error sending message:', error);
      }
    });
  }

  downloadAttachment(attachmentId: number, fileName: string): void {
    this.messageService.downloadAttachment(attachmentId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading attachment:', error);
        this.error = 'Failed to download attachment';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/messages']);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }
}
