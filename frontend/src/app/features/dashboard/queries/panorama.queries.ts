import { DashboardQuery } from '../dashboard.models';
import { count, rate, pair } from './chart-data';

export const PANORAMA_QUERIES: readonly DashboardQuery[] = [
  {
    id: 'institucion',
    label: 'Por tipo de institución',
    title: 'Aspirantes por tipo de institución',
    hint: 'M01 · correlativos distintos por tipo.',
    chart: 'donut',
    chartData: (d) => count(d.aspirantesPorInstitucion),
  },
  {
    id: 'sexo-institucion',
    label: 'Por sexo e institución',
    title: 'Aspirantes por sexo e institución',
    hint: 'M12 · correlativos distintos dentro de cada combinación.',
    chart: 'bar',
    chartData: (d) => pair(d.aspirantesPorSexoInstitucion),
  },
  {
    id: 'resultados-anio',
    label: 'Resultados por año',
    title: 'Resultados por año de ingreso',
    hint: 'M20 · distribución de evaluaciones por cohorte.',
    chart: 'donut',
    chartData: (d) => [
      {
        label: 'Aprobadas',
        value: d.resultadosPorAnio.reduce((sum, row) => sum + row.aprobadas, 0),
      },
      {
        label: 'No aprobadas',
        value: d.resultadosPorAnio.reduce((sum, row) => sum + row.noAprobadas, 0),
      },
    ],
  },
];
