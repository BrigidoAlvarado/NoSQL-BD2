import { CategoryField } from '../../shared/category-filters';
import { DashboardData } from './dashboard.models';

interface DashboardFilter { source: Exclude<keyof DashboardData, 'resumen'>; fields: CategoryField[]; }
const field = (key: string, label: string): CategoryField => ({key, label});
/** Una entrada por consulta; agregar filtros no modifica la agregación MongoDB. */
export const DASHBOARD_FILTERS: Record<string, DashboardFilter> = {
  M01: { source: 'aspirantesPorInstitucion', fields: [field('etiqueta', 'Institución')] },
  M02: { source: 'aprobadasPorMateria', fields: [field('etiqueta', 'Materia')] },
  M03: { source: 'aprobadosPorCarreraAnio', fields: [field('carrera', 'Carrera'), field('anioIngreso', 'Año')] },
  M04: { source: 'aprobacionPorMateria', fields: [field('etiqueta', 'Materia')] },
  M05: { source: 'edadPorCarrera', fields: [field('etiqueta', 'Carrera')] },
  M06: { source: 'resumenCarreras', fields: [field('carrera', 'Carrera'), field('anioIngreso', 'Año')] },
  M07: { source: 'edadAprobadosPorCarreraInstitucion', fields: [field('primeraEtiqueta', 'Carrera'), field('segundaEtiqueta', 'Institución')] },
  M08: { source: 'aprobadosPorMunicipioCarrera', fields: [field('primeraEtiqueta', 'Departamento'), field('segundaEtiqueta', 'Municipio'), field('terceraEtiqueta', 'Carrera')] },
  M09: { source: 'evaluacionesPublicasPorMesMateria', fields: [field('primeraEtiqueta', 'Mes'), field('segundaEtiqueta', 'Materia')] },
  M10: { source: 'topCarrerasJuveniles', fields: [field('etiqueta', 'Carrera')] },
  M12: { source: 'aspirantesPorSexoInstitucion', fields: [field('primeraEtiqueta', 'Sexo'), field('segundaEtiqueta', 'Institución')] },
  M13: { source: 'aprobacionPorEdad', fields: [field('edad', 'Edad')] },
  M14: { source: 'oportunidadesPorMateria', fields: [field('materia', 'Materia')] },
  M16: { source: 'noAprobaronPrimerIntentoPorCarrera', fields: [field('etiqueta', 'Carrera')] },
  M17: { source: 'topMunicipios', fields: [field('primeraEtiqueta', 'Departamento'), field('segundaEtiqueta', 'Municipio')] },
  M18: { source: 'aprobacionPorInstitucion', fields: [field('etiqueta', 'Institución')] },
  M19: { source: 'carrerasMasDemandadasPorDepartamento', fields: [field('primeraEtiqueta', 'Departamento'), field('segundaEtiqueta', 'Carrera')] },
  M20: { source: 'resultadosPorAnio', fields: [field('anioIngreso', 'Año')] },
};
