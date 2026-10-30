import { ChartDatum } from '../dashboard/dashboard.models';
import { displayCareerName } from '../../shared/career-name.pipe';
import { displayMunicipality } from '../../shared/municipality-name.pipe';
import { QueryData } from './graph.models';
import { columnLabel } from './graph.presentation';

export interface GraphChartDefinition {
  categories: string[];
  metrics: string[];
}
/** Separa unidades: los promedios nunca se mezclan con cantidades de personas. */
export const GRAPH_CHARTS: Record<string, GraphChartDefinition> = {
  G01: { categories: ['carrera'], metrics: ['evaluaciones'] },
  G02: { categories: ['materia', 'resultado'], metrics: ['evaluaciones'] },
  G03: { categories: ['departamento', 'carrera'], metrics: ['aspirantes'] },
  G04: { categories: ['carreraOrigen', 'carreraDestino'], metrics: ['aspirantes'] },
  G05: { categories: ['anioIngreso', 'carrera', 'materia', 'resultado'], metrics: ['evaluaciones'] },
  G06: { categories: ['tipoInstitucion', 'materia', 'resultado'], metrics: ['evaluaciones'] },
  G07: { categories: ['anioIngreso', 'materia'], metrics: ['aspirantesReintentaron', 'promedioOportunidades'] },
  G08: { categories: ['departamento', 'municipio', 'carrera'], metrics: ['aspirantes'] },
  G09: { categories: ['sexo', 'carrera', 'materia', 'resultado'], metrics: ['evaluaciones'] },
  G10: { categories: ['carrera'], metrics: ['aspirantes', 'evaluaciones'] },
};

export function graphChartRows(result: QueryData, metric: string): ChartDatum[] {
  const definition = GRAPH_CHARTS[result.id];
  if (!definition || !definition.metrics.includes(metric)) return [];
  return result.filas
    .filter(row => typeof row[metric] === 'number' && Number.isFinite(row[metric]) && (row[metric] as number) >= 0)
    .map(row => ({
      label: definition.categories.map(key => {
        const value = String(row[key] ?? 'Sin registro');
        if (key === 'municipio' && typeof row['departamento'] === 'string') {
          return displayMunicipality(value, row['departamento']);
        }
        return displayCareerName(value);
      }).join(' · '),
      value: row[metric] as number,
    }))
    .sort((a, b) => b.value - a.value);
}

export function graphMetricLabel(metric: string): string {
  return metric === 'promedioOportunidades' ? 'Promedio de oportunidades por aspirante' : columnLabel(metric);
}
