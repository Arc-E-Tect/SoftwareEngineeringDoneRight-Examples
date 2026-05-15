import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface PersonResponse {
  firstName: string;
  lastName: string;
}

export interface RelationshipRequest {
  fromFirstName: string;
  fromLastName: string;
  toFirstName: string;
  toLastName: string;
  type: string;
}

export interface RelationshipResponse {
  id?: number;
  fromPerson?: PersonResponse;
  toPerson?: PersonResponse;
  type?: string;
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
      this.http.post<RelationshipResponse>('/v1/familyties/relationships', rel, { headers: this.jsonHeaders })
    );
  }

  findRelations(type: string, lastName: string): Promise<PersonResponse[]> {
    return firstValueFrom(
      this.http.get<PersonResponse[]>(
        `/v1/familyties/relationships/${encodeURIComponent(type)}/lastnames/${encodeURIComponent(lastName)}`,
        { headers: this.getHeaders }
      )
    );
  }
}
