import { Component, OnInit, inject, signal } from '@angular/core';
import { KeyValuePipe } from '@angular/common';
import { SystemAdminService, HealthResponse, VersionResponse } from './system-admin.service';

@Component({
  selector: 'app-system-admin',
  standalone: true,
  imports: [KeyValuePipe],
  templateUrl: './system-admin.component.html',
  styleUrl: './system-admin.component.css'
})
export class SystemAdminComponent implements OnInit {
  private readonly service = inject(SystemAdminService);

  protected readonly healthStatus = signal<HealthResponse | null>(null);
  protected readonly versionInfo = signal<VersionResponse | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  ngOnInit(): void {
    this.load();
  }

  protected refresh(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    Promise.all([
      this.service.getHealth().then(h => this.healthStatus.set(h)),
      this.service.getVersion().then(v => this.versionInfo.set(v))
    ])
      .catch(err => this.error.set(err?.message ?? 'Failed to load system information'))
      .finally(() => this.loading.set(false));
  }
}
