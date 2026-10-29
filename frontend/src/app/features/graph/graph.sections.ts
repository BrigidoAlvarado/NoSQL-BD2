import { AnalysisSection } from '../../shared/analysis-section';

export interface GraphSection extends AnalysisSection { queries: readonly string[]; }

export const GRAPH_SECTIONS: readonly GraphSection[] = [
  {id: 'grafo', label: 'Explorar grafo', description: 'Conexiones y filtros', question: 'Explora las relaciones de una muestra de evaluaciones.', queries: []},
  {id: 'carreras', label: 'Carreras', description: 'Demanda por carrera', question: '¿Qué carreras concentran evaluaciones?', queries: ['G01']},
  {id: 'trayectorias', label: 'Trayectorias', description: 'Cambios, reintentos y resultados', question: '¿Qué aspirantes cambian de carrera, reintentan o presentan resultados distintos?', queries: ['G04', 'G07', 'G10']},
  {id: 'resultados', label: 'Resultados', description: 'Materias, años y sexo', question: '¿Cómo se distribuyen los resultados por materia, cohorte, carrera y sexo?', queries: ['G02', 'G05', 'G09']},
  {id: 'procedencia', label: 'Procedencia', description: 'Instituciones y ubicación', question: '¿Cómo se conectan los resultados, la carrera y la procedencia educativa?', queries: ['G03', 'G06', 'G08']},
];
