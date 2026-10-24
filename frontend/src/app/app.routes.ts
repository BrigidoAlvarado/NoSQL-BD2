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
    data: {
      eyebrow: 'CONSULTA DE INFORMACIÓN', heading: 'Búsqueda de aspirantes',
      description: 'Encuentra evaluaciones e historial de los aspirantes de primer ingreso.',
      message: 'La búsqueda estará disponible cuando se integre el servicio de consultas.',
      criteria: ['Correlativo del aspirante', 'Carrera objetivo', 'Tipo de institución educativa', 'Materia', 'Municipio', 'Departamento'],
    },
    loadComponent: () => import('./shared/pending-page').then((m) => m.PendingPage),
  },
  { path: 'grafos', title: 'Explorador de grafos | Admisiones USAC', loadComponent: () => import('./features/graph/graph').then((m) => m.GraphExplorer) },
  { path: '**', redirectTo: 'estadisticas' },
];
