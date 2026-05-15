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
  protected addForm = { fromFirstName: '', fromLastName: '', toFirstName: '', toLastName: '', type: '' };
  protected addResult = signal<RelationshipResponse | null>(null);
  protected addError = signal<string | null>(null);
  protected addLoading = signal(false);

  // Find Relations
  protected findForm = { type: '', lastName: '' };
  protected findResults = signal<PersonResponse[]>([]);
  protected findError = signal<string | null>(null);
  protected findLoading = signal(false);
  protected findDone = signal(false);

  protected isAddFormValid(): boolean {
    const f = this.addForm;
    return !!(f.fromFirstName.trim() && f.fromLastName.trim() && f.toFirstName.trim() && f.toLastName.trim() && f.type.trim());
  }

  protected addRelationship(): void {
    if (!this.isAddFormValid()) return;
    this.addLoading.set(true);
    this.addResult.set(null);
    this.addError.set(null);
    this.service
      .addRelationship({
        fromFirstName: this.addForm.fromFirstName.trim(),
        fromLastName: this.addForm.fromLastName.trim(),
        toFirstName: this.addForm.toFirstName.trim(),
        toLastName: this.addForm.toLastName.trim(),
        type: this.addForm.type.trim()
      })
      .then(r => this.addResult.set(r))
      .catch(err => this.addError.set(err?.error?.message ?? err?.message ?? 'Failed to add relationship'))
      .finally(() => this.addLoading.set(false));
  }

  protected findRelations(): void {
    if (!this.findForm.type.trim() || !this.findForm.lastName.trim()) return;
    this.findLoading.set(true);
    this.findResults.set([]);
    this.findError.set(null);
    this.findDone.set(false);
    this.service
      .findRelations(this.findForm.type.trim(), this.findForm.lastName.trim())
      .then(r => { this.findResults.set(r); this.findDone.set(true); })
      .catch(err => this.findError.set(err?.error?.message ?? err?.message ?? 'No relations found'))
      .finally(() => this.findLoading.set(false));
  }
}
