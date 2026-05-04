import { Component, OnInit, inject, signal } from '@angular/core';
import { SystemService, HealthResponse } from './system.service';

@Component({
  selector: 'app-system',
  standalone: true,
  imports: [],
  templateUrl: './system.component.html',
  styleUrl: './system.component.css'
})
export class SystemComponent implements OnInit {
  private readonly service = inject(SystemService);

  protected readonly healthStatus = signal<HealthResponse | null>(null);
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
    this.service.getHealth()
      .then(h => this.healthStatus.set(h))
      .catch(err => this.error.set(err?.message ?? 'Failed to reach microservice'))
      .finally(() => this.loading.set(false));
  }
}
