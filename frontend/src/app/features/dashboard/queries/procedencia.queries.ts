import { displayMunicipality } from '../../../shared/municipality-name.pipe';
import { DashboardQuery } from '../dashboard.models';
import { count, rate, pair } from './chart-data';

export const PROCEDENCIA_QUERIES: readonly DashboardQuery[] = [
  {
    id: 'municipio-carrera',
    label: 'Aprobados por municipio',
    title: 'Aprobados por municipio y carrera',
    mongoId: 'M08',
    hint: 'municipio conserva su departamento.',
    chart: 'bar',
    chartData: (d) =>
      d.aprobadosPorMunicipioCarrera.map((row) => ({
        label: `${row.primeraEtiqueta} · ${displayMunicipality(row.segundaEtiqueta, row.primeraEtiqueta)} · ${row.terceraEtiqueta}`,
        value: row.cantidad,
      })),
  },
  {
    id: 'publicas-mes',
    label: 'Públicas por mes y materia',
    title: 'Evaluaciones públicas por mes y materia',
    mongoId: 'M09',
    hint: 'fecha de asignación.',
    chart: 'bar',
    chartData: (d) => pair(d.evaluacionesPublicasPorMesMateria),
  },
  {
    id: 'municipios',
    label: 'Top municipios',
    title: 'Top municipios de procedencia',
    mongoId: 'M17',
    hint: 'top 5, conserva departamento.',
    chart: 'bar',
    chartData: (d) => pair(d.topMunicipios.map(row => ({ ...row, segundaEtiqueta: displayMunicipality(row.segundaEtiqueta, row.primeraEtiqueta) }))),
  },
  {
    id: 'demanda-departamento',
    label: 'Demanda por departamento',
    title: 'Carreras más demandadas por departamento',
    mongoId: 'M19',
    hint: 'incluye empates.',
    chart: 'bar',
    chartData: (d) => pair(d.carrerasMasDemandadasPorDepartamento),
  },
];
