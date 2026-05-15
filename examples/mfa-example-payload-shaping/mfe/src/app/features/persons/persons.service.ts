import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface PersonRequest {
  voornaam: string;
  achternaam: string;
}

export interface PersonResponse {
  voornaam: string;
  achternaam: string;
}

@Injectable({ providedIn: 'root' })
export class PersonsService {
  private readonly http = inject(HttpClient);
  private readonly jsonHeaders = new HttpHeaders({
    Accept: 'application/json',
    'Content-Type': 'application/json'
  });
  private readonly getHeaders = new HttpHeaders({ Accept: 'application/json' });

  addPerson(person: PersonRequest): Promise<PersonResponse> {
    return firstValueFrom(
      this.http.post<PersonResponse>('/v1/familyties', person, { headers: this.jsonHeaders })
    );
  }

  findByLastName(lastName: string): Promise<PersonResponse[]> {
    return firstValueFrom(
      this.http.get<PersonResponse[]>(
        `/v1/familyties/lastnames/${encodeURIComponent(lastName)}`,
        { headers: this.getHeaders }
      )
    );
  }

  deletePerson(lastName: string, firstName: string): Promise<void> {
    return firstValueFrom(
      this.http.delete<void>(
        `/v1/familyties/lastnames/${encodeURIComponent(lastName)}?firstname=${encodeURIComponent(firstName)}`
      )
    );
  }
}
