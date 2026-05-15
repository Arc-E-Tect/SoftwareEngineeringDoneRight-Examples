import { Component, OnInit, inject, signal } from '@angular/core';
import { RelationshipService, HealthResponse, PersonResponse } from './relationship.service';

@Component({
  selector: 'app-relationship',
  standalone: true,
  imports: [],
  templateUrl: './relationship.component.html',
  styleUrl: './relationship.component.css'
})
export class RelationshipComponent implements OnInit {
  private readonly service = inject(RelationshipService);

  protected readonly healthStatus = signal<HealthResponse | null>(null);
  protected readonly relationships = signal<PersonResponse[]>([]);
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
      this.service.getRelationships('Smith').then(r => this.relationships.set(r))
    ])
      .catch(err => this.error.set(err?.message ?? 'Failed to load relationship information'))
      .finally(() => this.loading.set(false));
  }
}
