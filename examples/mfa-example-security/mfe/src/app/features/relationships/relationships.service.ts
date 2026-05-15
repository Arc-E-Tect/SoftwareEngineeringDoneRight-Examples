import { Injectable } from '@angular/core';

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

/**
 * Intentionally uses the native Fetch API instead of Angular's HttpClient so that
 * requests bypass the apiKeyInterceptor.  This demonstrates what happens when a
 * caller reaches the MFA without a validated API-key header forwarded by the
 * API Gateway – the MFA returns 403 Forbidden.
 */
@Injectable({ providedIn: 'root' })
export class RelationshipsService {

  addRelationship(rel: RelationshipRequest): Promise<RelationshipResponse> {
    return fetch('/v1/dutch/familyties/relationships', {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(rel)
    }).then(response => {
      if (!response.ok) {
        return response.json().then(err => Promise.reject(err));
      }
      return response.json() as Promise<RelationshipResponse>;
    });
  }

  findRelations(type: string, lastName: string): Promise<PersonResponse[]> {
    return fetch(
      `/v1/dutch/familyties/relationships/${encodeURIComponent(type)}/lastnames/${encodeURIComponent(lastName)}`,
      { headers: { 'Accept': 'application/json' } }
    ).then(response => {
      if (!response.ok) {
        return response.json().then(err => Promise.reject(err));
      }
      return response.json() as Promise<PersonResponse[]>;
    });
  }
}

