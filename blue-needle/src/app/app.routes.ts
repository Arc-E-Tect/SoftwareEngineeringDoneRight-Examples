import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/system-admin', pathMatch: 'full' },
  {
    path: 'system-admin',
    loadChildren: () => import('./features/system-admin/system-admin.routes').then(m => m.routes)
  },
  {
    path: 'user-account',
    loadChildren: () => import('./features/user-account/user-account.routes').then(m => m.routes)
  },
  {
    path: 'auth-server',
    loadChildren: () => import('./features/auth-server/auth-server.routes').then(m => m.routes)
  },
  {
    path: 'relationship',
    loadChildren: () => import('./features/relationship/relationship.routes').then(m => m.routes)
  },
  {
    path: 'social-network',
    loadChildren: () => import('./features/social-network/social-network.routes').then(m => m.routes)
  }
];
