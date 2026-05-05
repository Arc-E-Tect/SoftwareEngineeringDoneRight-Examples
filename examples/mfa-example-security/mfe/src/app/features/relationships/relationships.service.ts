import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface PersonResponse {
  voornaam: string;
  achternaam: string;
}

export interface RelationshipRequest {
  vanVoornaam: string;
  vanAchternaam: string;
  naarVoornaam: string;
  naarAchternaam: string;
  soort: string;
}

export interface RelationshipResponse {
  id?: number;
  vanPersoon?: PersonResponse;
  naarPersoon?: PersonResponse;
  soort?: string;
}

@Injectable({ providedIn: 'root' })
export class RelationshipsService {
  private readonly http = inject(HttpClient);
  private readonly jsonHeaders = new HttpHeaders({
    Accept: 'application/json',
    'Content-Type': 'application/json'
  });
  private readonly getHeaders = new HttpHeaders({ Accept: 'application/json' });

  addRelationship(rel: RelationshipRequest): Promise<RelationshipResponse> {
    return firstValueFrom(
      this.http.post<RelationshipResponse>('/v1/dutch/familyties/relationships', rel, { headers: this.jsonHeaders })
    );
  }

  findRelations(type: string, lastName: string): Promise<PersonResponse[]> {
    return firstValueFrom(
      this.http.get<PersonResponse[]>(
        `/v1/dutch/familyties/relationships/${encodeURIComponent(type)}/lastnames/${encodeURIComponent(lastName)}`,
        { headers: this.getHeaders }
      )
    );
  }
}
