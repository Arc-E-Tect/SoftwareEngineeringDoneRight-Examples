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
export class UserAccountService {
  private readonly http = inject(HttpClient);
  private readonly headers = new HttpHeaders({ Accept: 'application/json' });

  getHealth(): Promise<HealthResponse> {
    return firstValueFrom(
      this.http.get<HealthResponse>('/user-account/actuator/health', { headers: this.headers })
    );
  }

  getVersion(): Promise<VersionResponse> {
    return firstValueFrom(
      this.http.get<VersionResponse>('/user-account/api/version', { headers: this.headers })
    );
  }
}
