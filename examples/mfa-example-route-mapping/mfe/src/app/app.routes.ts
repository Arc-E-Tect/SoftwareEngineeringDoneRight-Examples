import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/persons', pathMatch: 'full' },
  {
    path: 'persons',
    loadChildren: () => import('./features/persons/persons.routes').then(m => m.routes)
  },
  {
    path: 'relationships',
    loadChildren: () => import('./features/relationships/relationships.routes').then(m => m.routes)
  },
  {
    path: 'system',
    loadChildren: () => import('./features/system/system.routes').then(m => m.routes)
  }
];
