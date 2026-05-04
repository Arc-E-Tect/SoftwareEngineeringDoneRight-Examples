import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RelationshipsService, PersonResponse, RelationshipResponse } from './relationships.service';

@Component({
  selector: 'app-relationships',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './relationships.component.html',
  styleUrl: './relationships.component.css'
})
export class RelationshipsComponent {
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
