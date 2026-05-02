import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/relationship', pathMatch: 'full' },
  {
    path: 'relationship',
    loadChildren: () => import('./features/relationship/relationship.routes').then(m => m.routes)
  },
  {
    path: 'social-network',
    loadChildren: () => import('./features/social-network/social-network.routes').then(m => m.routes)
  }
];
