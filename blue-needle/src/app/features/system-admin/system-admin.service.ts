import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface HealthResponse {
  status: string;
}

export interface DependencyVersion {
  versionName: string;
  versionCode: number;
}

export interface VersionResponse {
  versionName: string;
  versionCode: number;
  dependencies?: Record<string, DependencyVersion>;
}

@Injectable({ providedIn: 'root' })
export class SystemAdminService {
  private readonly http = inject(HttpClient);
  private readonly headers = new HttpHeaders({ Accept: 'application/json' });

  getHealth(): Promise<HealthResponse> {
    return firstValueFrom(
      this.http.get<HealthResponse>('/actuator/health', { headers: this.headers })
    );
  }

  getVersion(): Promise<VersionResponse> {
    return firstValueFrom(
      this.http.get<VersionResponse>('/api/iff/', { headers: this.headers })
    );
  }
}
