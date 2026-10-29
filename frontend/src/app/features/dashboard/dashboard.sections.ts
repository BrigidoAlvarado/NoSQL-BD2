import { AnalysisSection } from '../../shared/analysis-section';

export const DASHBOARD_SECTIONS: readonly AnalysisSection[] = [
  {id: 'panorama', label: 'Panorama', description: 'Instituciones, sexo y años', question: '¿Quiénes se evalúan y cómo cambian los resultados por año?'},
  {id: 'aprobacion', label: 'Aprobación', description: 'Materias, resultados e intentos', question: '¿Qué materias se aprueban y cuántas oportunidades se utilizan?'},
  {id: 'carreras', label: 'Carreras', description: 'Demanda y resumen por carrera', question: '¿Qué carreras buscan los aspirantes y cuáles son sus resultados?'},
  {id: 'perfil', label: 'Perfil y edades', description: 'Edades y desempeño', question: '¿Cómo se relaciona la edad con la carrera y la aprobación?'},
  {id: 'procedencia', label: 'Procedencia', description: 'Ubicación e instituciones', question: '¿De dónde provienen los aspirantes y qué muestran sus instituciones educativas?'},
];
