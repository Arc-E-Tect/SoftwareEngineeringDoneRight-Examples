import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface HealthResponse {
  status: string;
}

export interface VersionResponse {
  versionName: string;
  versionCode: number;
}

@Injectable({ providedIn: 'root' })
export class RelationshipService {
  private readonly http = inject(HttpClient);
  private readonly headers = new HttpHeaders({ Accept: 'application/json' });

  getHealth(): Promise<HealthResponse> {
    return firstValueFrom(
      this.http.get<HealthResponse>('/relationship/actuator/health', { headers: this.headers })
    );
  }

  getVersion(): Promise<VersionResponse> {
    return firstValueFrom(
      this.http.get<VersionResponse>('/relationship/api/version', { headers: this.headers })
    );
  }
}
