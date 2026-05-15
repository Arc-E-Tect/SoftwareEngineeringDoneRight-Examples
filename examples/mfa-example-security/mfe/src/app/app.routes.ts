import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/persons', pathMatch: 'full' },
  {
    path: 'persons',
    canActivate: [authGuard],
    loadChildren: () => import('./features/persons/persons.routes').then(m => m.routes)
  },
  {
    path: 'relationships',
    canActivate: [authGuard],
    loadChildren: () => import('./features/relationships/relationships.routes').then(m => m.routes)
  },
  {
    path: 'system',
    loadChildren: () => import('./features/system/system.routes').then(m => m.routes)
  }
];
