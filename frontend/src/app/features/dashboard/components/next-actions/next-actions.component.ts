import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DashboardService, NextAction } from '../../services/dashboard.service';

@Component({
  selector: 'app-next-actions',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './next-actions.component.html',
  styleUrls: ['./next-actions.component.scss']
})
export class NextActionsComponent implements OnInit {
  actions: NextAction[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadNextActions();
  }

  loadNextActions(): void {
    this.loading = true;
    this.error = null;

    this.dashboardService.getNextActions().subscribe({
      next: (actions) => {
        this.actions = actions;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load recommended actions';
        this.loading = false;
        console.error('Error loading next actions:', error);
      }
    });
  }

  handleAction(action: NextAction): void {
    this.router.navigateByUrl(action.actionUrl);
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getActionTypeColor(priority: string): string {
    switch (priority) {
      case 'HIGH': return '#C41030';
      case 'MEDIUM': return '#F59E0B';
      case 'LOW': return '#10B981';
      default: return '#6B7280';
    }
  }

  refresh(): void {
    this.loadNextActions();
  }
}
