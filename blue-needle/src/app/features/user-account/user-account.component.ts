import { Component, OnInit, inject, signal } from '@angular/core';
import { UserAccountService, HealthResponse, VersionResponse } from './user-account.service';

@Component({
  selector: 'app-user-account',
  standalone: true,
  imports: [],
  templateUrl: './user-account.component.html',
  styleUrl: './user-account.component.css'
})
export class UserAccountComponent implements OnInit {
  private readonly service = inject(UserAccountService);

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
      .catch(err => this.error.set(err?.message ?? 'Failed to load user account information'))
      .finally(() => this.loading.set(false));
  }
}

