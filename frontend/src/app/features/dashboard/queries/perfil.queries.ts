import { DashboardQuery } from '../dashboard.models';
import { count, rate, pair } from './chart-data';

export const PERFIL_QUERIES: readonly DashboardQuery[] = [
  {
    id: 'edad-carrera',
    label: 'Edad por carrera',
    title: 'Edad promedio por carrera',
    hint: 'M05 · una edad por aspirante y carrera.',
    chart: 'bar',
    chartData: (d) =>
      d.edadPorCarrera.map((row) => ({
        label: row.etiqueta,
        value: row.promedio,
      })),
  },
  {
    id: 'edad-aprobados',
    label: 'Edad de aprobados',
    title: 'Edad de aprobados por carrera e institución',
    hint: 'M07 · promedio sin ponderar intentos.',
    chart: 'bar',
    chartData: (d) =>
      d.edadAprobadosPorCarreraInstitucion.map((row) => ({
        label: `${row.primeraEtiqueta} · ${row.segundaEtiqueta}`,
        value: row.promedio,
      })),
  },
  {
    id: 'aprobacion-edad',
    label: 'Aprobación por edad',
    title: 'Aprobación por edad',
    hint: 'M13 · solo edades válidas.',
    chart: 'bar',
    chartData: (d) =>
      d.aprobacionPorEdad.map((row) => ({
        label: `${row.edad} años`,
        value: row.porcentajeAprobacion,
      })),
  },
];
