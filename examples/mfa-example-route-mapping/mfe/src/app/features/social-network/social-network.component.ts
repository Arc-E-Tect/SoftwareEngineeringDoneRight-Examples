import { Component, OnInit, inject, signal } from '@angular/core';
import { SocialNetworkService, HealthResponse, PersonResponse } from './social-network.service';

@Component({
  selector: 'app-social-network',
  standalone: true,
  imports: [],
  templateUrl: './social-network.component.html',
  styleUrl: './social-network.component.css'
})
export class SocialNetworkComponent implements OnInit {
  private readonly service = inject(SocialNetworkService);

  protected readonly healthStatus = signal<HealthResponse | null>(null);
  protected readonly familyMembers = signal<PersonResponse[]>([]);
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
      this.service.getFamilyMembers('Smith').then(m => this.familyMembers.set(m))
    ])
      .catch(err => this.error.set(err?.message ?? 'Failed to load social network information'))
      .finally(() => this.loading.set(false));
  }
}
