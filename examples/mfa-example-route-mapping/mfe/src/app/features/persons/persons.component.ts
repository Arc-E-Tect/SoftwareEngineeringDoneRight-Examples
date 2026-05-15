import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PersonsService, PersonResponse } from './persons.service';

@Component({
  selector: 'app-persons',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './persons.component.html',
  styleUrl: './persons.component.css'
})
export class PersonsComponent {
  private readonly service = inject(PersonsService);

  // Add Person
  protected addForm = { voornaam: '', achternaam: '' };
  protected addResult = signal<PersonResponse | null>(null);
  protected addError = signal<string | null>(null);
  protected addLoading = signal(false);

  // Find by Last Name
  protected searchAchternaam = '';
  protected searchResults = signal<PersonResponse[]>([]);
  protected searchError = signal<string | null>(null);
  protected searchLoading = signal(false);
  protected searchDone = signal(false);

  // Delete Person
  protected deleteForm = { voornaam: '', achternaam: '' };
  protected deleteSuccess = signal(false);
  protected deleteError = signal<string | null>(null);
  protected deleteLoading = signal(false);

  protected addPerson(): void {
    if (!this.addForm.voornaam.trim() || !this.addForm.achternaam.trim()) return;
    this.addLoading.set(true);
    this.addResult.set(null);
    this.addError.set(null);
    this.service
      .addPerson({ voornaam: this.addForm.voornaam.trim(), achternaam: this.addForm.achternaam.trim() })
      .then(r => this.addResult.set(r))
      .catch(err => this.addError.set(err?.error?.message ?? err?.message ?? 'Failed to add person'))
      .finally(() => this.addLoading.set(false));
  }

  protected findPersons(): void {
    if (!this.searchAchternaam.trim()) return;
    this.searchLoading.set(true);
    this.searchResults.set([]);
    this.searchError.set(null);
    this.searchDone.set(false);
    this.service
      .findByLastName(this.searchAchternaam.trim())
      .then(r => { this.searchResults.set(r); this.searchDone.set(true); })
      .catch(err => this.searchError.set(err?.error?.message ?? err?.message ?? 'No persons found'))
      .finally(() => this.searchLoading.set(false));
  }

  protected deletePerson(): void {
    if (!this.deleteForm.voornaam.trim() || !this.deleteForm.achternaam.trim()) return;
    this.deleteLoading.set(true);
    this.deleteSuccess.set(false);
    this.deleteError.set(null);
    this.service
      .deletePerson(this.deleteForm.achternaam.trim(), this.deleteForm.voornaam.trim())
      .then(() => this.deleteSuccess.set(true))
      .catch(err => this.deleteError.set(err?.error?.message ?? err?.message ?? 'Failed to delete person'))
      .finally(() => this.deleteLoading.set(false));
  }
}
