import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface HealthResponse {
  status: string;
}

export interface PersonResponse {
  firstName: string;
  lastName: string;
}

@Injectable({ providedIn: 'root' })
export class RelationshipService {
  private readonly http = inject(HttpClient);
  private readonly headers = new HttpHeaders({ Accept: 'application/json' });

  getHealth(): Promise<HealthResponse> {
    return firstValueFrom(
      this.http.get<HealthResponse>('/actuator/health', { headers: this.headers })
    );
  }

  getRelationships(lastName: string): Promise<PersonResponse[]> {
    return firstValueFrom(
      this.http.get<PersonResponse[]>(
        `/v1/dutch/familyties/relationships/child/lastnames/${encodeURIComponent(lastName)}`,
        { headers: this.headers }
      )
    );
  }
}
