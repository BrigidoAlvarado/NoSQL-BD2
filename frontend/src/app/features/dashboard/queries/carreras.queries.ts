import { DashboardQuery } from '../dashboard.models';
import { count, rate, pair } from './chart-data';

export const CARRERAS_QUERIES: readonly DashboardQuery[] = [
  {
    id: 'aprobados-carrera',
    label: 'Aprobados por carrera y año',
    title: 'Aspirantes aprobados por carrera y año',
    mongoId: 'M03',
    hint: 'correlativos con alguna aprobación.',
    chart: 'bar',
    chartData: (d) =>
      d.aprobadosPorCarreraAnio.map((row) => ({
        label: `${row.carrera} · ${row.anioIngreso}`,
        value: row.aspirantes,
      })),
  },
  {
    id: 'resumen-carrera',
    label: 'Resumen por carrera',
    title: 'Resumen por carrera',
    mongoId: 'M06',
    hint: 'colección materializada resumen_carrera.',
    chart: 'bar',
    chartData: (d) =>
      d.resumenCarreras.map((row) => ({
        label: `${row.carrera} · ${row.anioIngreso}`,
        value: row.aspirantesDistintos,
      })),
  },
  {
    id: 'juveniles',
    label: 'Top carreras 16–18',
    title: 'Top carreras de aspirantes de 16 a 18',
    mongoId: 'M10',
    hint: 'top 5, excluye carrera desconocida.',
    chart: 'bar',
    chartData: (d) => count(d.topCarrerasJuveniles),
  },
  {
    id: 'primer-intento',
    label: 'Primer intento no aprobado',
    title: 'No aprobaron primer intento',
    mongoId: 'M16',
    hint: 'oportunidad 1 por carrera.',
    chart: 'bar',
    chartData: (d) => count(d.noAprobaronPrimerIntentoPorCarrera),
  },
];
