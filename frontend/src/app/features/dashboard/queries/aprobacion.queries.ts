import { DashboardQuery } from '../dashboard.models';
import { count, rate, pair } from './chart-data';

export const APROBACION_QUERIES: readonly DashboardQuery[] = [
  {
    id: 'aprobadas-materia',
    label: 'Aprobadas por materia',
    title: 'Evaluaciones aprobadas por materia',
    hint: 'M02 · evaluaciones, no aspirantes.',
    chart: 'bar',
    chartData: (d) => count(d.aprobadasPorMateria),
  },
  {
    id: 'porcentaje-materia',
    label: 'Porcentaje por materia',
    title: 'Porcentaje de aprobación por materia',
    hint: 'M04 · aprobadas entre evaluaciones de la materia.',
    chart: 'bar',
    chartData: (d) => rate(d.aprobacionPorMateria),
  },
  {
    id: 'porcentaje-institucion',
    label: 'Por institución',
    title: 'Aprobación por tipo de institución',
    hint: 'M18 · porcentaje de evaluaciones aprobadas.',
    chart: 'bar',
    chartData: (d) => rate(d.aprobacionPorInstitucion),
  },
  {
    id: 'oportunidades',
    label: 'Oportunidades por materia',
    title: 'Oportunidades usadas por materia',
    hint: 'M14 · oportunidades distintas por aspirante, cohorte y materia.',
    chart: 'bar',
    chartData: (d) =>
      d.oportunidadesPorMateria.map((row) => ({
        label: row.materia,
        value: row.promedio,
      })),
  },
];
