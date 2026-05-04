import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface HealthResponse {
  status: string;
}

@Injectable({ providedIn: 'root' })
export class SystemService {
  private readonly http = inject(HttpClient);

  getHealth(): Promise<HealthResponse> {
    return firstValueFrom(
      this.http.get<HealthResponse>('/actuator/health', {
        headers: new HttpHeaders({ Accept: 'application/json' })
      })
    );
  }
}
