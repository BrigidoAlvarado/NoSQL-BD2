import { DashboardData } from './dashboard.models';

export function isDashboard(value: unknown): value is DashboardData {
  if (typeof value !== 'object' || value === null) return false;
  const data = value as Record<string, unknown>;
  const arrays = [
    'aspirantesPorInstitucion',
    'aprobadasPorMateria',
    'aprobacionPorMateria',
    'aspirantesPorSexoInstitucion',
    'aprobacionPorInstitucion',
    'resultadosPorAnio',
    'aprobadosPorCarreraAnio',
    'edadPorCarrera',
    'resumenCarreras',
    'edadAprobadosPorCarreraInstitucion',
    'aprobadosPorMunicipioCarrera',
    'evaluacionesPublicasPorMesMateria',
    'topCarrerasJuveniles',
    'aprobacionPorEdad',
    'oportunidadesPorMateria',
    'noAprobaronPrimerIntentoPorCarrera',
    'topMunicipios',
    'carrerasMasDemandadasPorDepartamento',
  ];
  return (
    typeof data['resumen'] === 'object' &&
    data['resumen'] !== null &&
    arrays.every((key) => Array.isArray(data[key]))
  );
}
