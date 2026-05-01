import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthServerService, HealthResponse, VersionResponse } from './auth-server.service';

@Component({
  selector: 'app-auth-server',
  standalone: true,
  imports: [],
  templateUrl: './auth-server.component.html',
  styleUrl: './auth-server.component.css'
})
export class AuthServerComponent implements OnInit {
  private readonly service = inject(AuthServerService);

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
      .catch(err => this.error.set(err?.message ?? 'Failed to load auth server information'))
      .finally(() => this.loading.set(false));
  }
}
