import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface NavItem {
  label: string;
  route: string;
  icon?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './app-sidebar.component.html',
  styleUrls: ['./app-sidebar.component.scss']
})
export class AppSidebarComponent {
  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard' },
    { label: 'Patients', route: '/patients' },
    { label: 'Enrollment', route: '/patients' },
    { label: 'Benefits Investigation', route: '/benefits' },
    { label: 'Forms & Resources', route: '/forms' },
    { label: 'Secure Messages', route: '/messages' },
    { label: 'Admin Console', route: '/admin' },
  ];
}
