import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MessageService, MessageThread } from '../../services/message.service';

@Component({
  selector: 'app-thread-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    MatPaginatorModule
  ],
  templateUrl: './thread-list.component.html',
  styleUrls: ['./thread-list.component.scss']
})
export class ThreadListComponent implements OnInit {
  threads: MessageThread[] = [];
  loading = false;
  error: string | null = null;

  // Pagination
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;

  constructor(
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadThreads();
  }

  loadThreads(): void {
    this.loading = true;
    this.error = null;

    this.messageService.getThreads(this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.threads = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load threads';
        this.loading = false;
        console.error('Error loading threads:', error);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadThreads();
  }

  openThread(threadId: number): void {
    this.router.navigate(['/messages/thread', threadId]);
  }

  composeMessage(): void {
    this.router.navigate(['/messages/compose']);
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'No messages';

    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString();
  }

  getTotalUnread(): number {
    return this.threads.reduce((sum, thread) => sum + thread.unreadCount, 0);
  }
}
