import { DashboardQuery } from '../dashboard.models';
import { count, rate, pair } from './chart-data';

export const APROBACION_QUERIES: readonly DashboardQuery[] = [
  {
    id: 'aprobadas-materia',
    label: 'Aprobadas por materia',
    title: 'Evaluaciones aprobadas por materia',
    mongoId: 'M02',
    hint: 'evaluaciones, no aspirantes.',
    chart: 'bar',
    chartData: (d) => count(d.aprobadasPorMateria),
  },
  {
    id: 'porcentaje-materia',
    label: 'Porcentaje por materia',
    title: 'Porcentaje de aprobación por materia',
    mongoId: 'M04',
    hint: 'aprobadas entre evaluaciones de la materia.',
    chart: 'bar',
    chartData: (d) => rate(d.aprobacionPorMateria),
  },
  {
    id: 'porcentaje-institucion',
    label: 'Por institución',
    title: 'Aprobación por tipo de institución',
    mongoId: 'M18',
    hint: 'porcentaje de evaluaciones aprobadas.',
    chart: 'bar',
    chartData: (d) => rate(d.aprobacionPorInstitucion),
  },
  {
    id: 'oportunidades',
    label: 'Oportunidades por materia',
    title: 'Oportunidades usadas por materia',
    mongoId: 'M14',
    hint: 'oportunidades distintas por aspirante, cohorte y materia.',
    chart: 'bar',
    chartData: (d) =>
      d.oportunidadesPorMateria.map((row) => ({
        label: row.materia,
        value: row.promedio,
      })),
  },
];
