import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RelationshipsService, PersonResponse, RelationshipResponse } from './relationships.service';

@Component({
  selector: 'app-relationships',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './relationships.component.html',
  styleUrl: './relationships.component.css'
})
export class RelationshipsComponent implements OnInit {
  private readonly service = inject(RelationshipsService);

  // Add Relationship
  protected addForm = { vanVoornaam: '', vanAchternaam: '', naarVoornaam: '', naarAchternaam: '', soort: '' };
  protected addResult = signal<RelationshipResponse | null>(null);
  protected addError = signal<string | null>(null);
  protected addLoading = signal(false);

  // Find Relations
  protected findForm = { soort: '', achternaam: '' };
  protected findResults = signal<PersonResponse[]>([]);
  protected findError = signal<string | null>(null);
  protected findLoading = signal(false);
  protected findDone = signal(false);

  ngOnInit(): void {
    // Immediately probe the relationships endpoint on load to demonstrate the 403 response.
    // The service uses native fetch() (no apiKeyInterceptor), so the MFA rejects the
    // request with 403 Forbidden – the point of this security example.
    this.findForm.soort = 'parent';
    this.findForm.achternaam = 'jansen';
    this.findRelations();
  }

  protected isAddFormValid(): boolean {
    const f = this.addForm;
    return !!(f.vanVoornaam.trim() && f.vanAchternaam.trim() && f.naarVoornaam.trim() && f.naarAchternaam.trim() && f.soort.trim());
  }

  protected addRelationship(): void {
    if (!this.isAddFormValid()) return;
    this.addLoading.set(true);
    this.addResult.set(null);
    this.addError.set(null);
    this.service
      .addRelationship({
        vanVoornaam: this.addForm.vanVoornaam.trim(),
        vanAchternaam: this.addForm.vanAchternaam.trim(),
        naarVoornaam: this.addForm.naarVoornaam.trim(),
        naarAchternaam: this.addForm.naarAchternaam.trim(),
        soort: this.addForm.soort.trim()
      })
      .then(r => this.addResult.set(r))
      .catch(err => this.addError.set(err?.error?.message ?? err?.message ?? 'Failed to add relationship'))
      .finally(() => this.addLoading.set(false));
  }

  protected findRelations(): void {
    if (!this.findForm.soort.trim() || !this.findForm.achternaam.trim()) return;
    this.findLoading.set(true);
    this.findResults.set([]);
    this.findError.set(null);
    this.findDone.set(false);
    this.service
      .findRelations(this.findForm.soort.trim(), this.findForm.achternaam.trim())
      .then(r => { this.findResults.set(r); this.findDone.set(true); })
      .catch(err => this.findError.set(err?.error?.message ?? err?.message ?? 'No relations found'))
      .finally(() => this.findLoading.set(false));
  }
}
