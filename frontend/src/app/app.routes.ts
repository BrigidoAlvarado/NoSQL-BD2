import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'estadisticas' },
  {
    path: 'estadisticas', title: 'Estadísticas | Admisiones USAC',
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    path: 'carga', title: 'Carga de datos | Admisiones USAC',
    loadComponent: () => import('./features/upload/upload').then((m) => m.Upload),
  },
  {
    path: 'busqueda', title: 'Búsqueda | Admisiones USAC',
    loadComponent: () => import('./features/search/search').then((m) => m.Search),
  },
  { path: 'grafos', title: 'Explorador de grafos | Admisiones USAC', loadComponent: () => import('./features/graph/graph').then((m) => m.GraphExplorer) },
  { path: '**', redirectTo: 'estadisticas' },
];
