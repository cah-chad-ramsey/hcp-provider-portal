import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';

interface AdminAction {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: string;
}

@Component({
  selector: 'app-admin-console',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatGridListModule
  ],
  templateUrl: './admin-console.component.html',
  styleUrls: ['./admin-console.component.scss']
})
export class AdminConsoleComponent {
  adminActions: AdminAction[] = [
    {
      title: 'Provider Affiliations',
      description: 'Review and approve provider affiliation requests',
      icon: 'badge',
      route: '/admin/affiliations',
      color: '#008E97'
    },
    {
      title: 'Programs & Services',
      description: 'Manage programs and add support services',
      icon: 'label',
      route: '/admin/programs',
      color: '#C41030'
    },
    {
      title: 'Forms & Resources',
      description: 'Upload and manage downloadable forms',
      icon: 'folder',
      route: '/admin/forms/upload',
      color: '#7C3AED'
    },
    {
      title: 'Audit Logs',
      description: 'View system audit logs and user activity',
      icon: 'history',
      route: '/admin/audit',
      color: '#059669'
    }
  ];

  constructor(private router: Router) {}

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}
