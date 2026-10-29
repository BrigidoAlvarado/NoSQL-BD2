import { DashboardQuery } from '../dashboard.models';
import { count, rate, pair } from './chart-data';

export const PROCEDENCIA_QUERIES: readonly DashboardQuery[] = [
  {
    id: 'municipio-carrera',
    label: 'Aprobados por municipio',
    title: 'Aprobados por municipio y carrera',
    hint: 'M08 · municipio conserva su departamento.',
    chart: 'bar',
    chartData: (d) =>
      d.aprobadosPorMunicipioCarrera.map((row) => ({
        label: `${row.segundaEtiqueta} · ${row.terceraEtiqueta}`,
        value: row.cantidad,
      })),
  },
  {
    id: 'publicas-mes',
    label: 'Públicas por mes y materia',
    title: 'Evaluaciones públicas por mes y materia',
    hint: 'M09 · fecha de asignación.',
    chart: 'bar',
    chartData: (d) => pair(d.evaluacionesPublicasPorMesMateria),
  },
  {
    id: 'municipios',
    label: 'Top municipios',
    title: 'Top municipios de procedencia',
    hint: 'M17 · top 5, conserva departamento.',
    chart: 'bar',
    chartData: (d) => pair(d.topMunicipios),
  },
  {
    id: 'demanda-departamento',
    label: 'Demanda por departamento',
    title: 'Carreras más demandadas por departamento',
    hint: 'M19 · incluye empates.',
    chart: 'bar',
    chartData: (d) => pair(d.carrerasMasDemandadasPorDepartamento),
  },
];
