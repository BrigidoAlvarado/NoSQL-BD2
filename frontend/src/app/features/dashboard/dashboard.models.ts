export interface CountByLabel {
  etiqueta: string;
  cantidad: number;
}
export interface RateByLabel {
  etiqueta: string;
  aprobadas: number;
  evaluaciones: number;
  porcentajeAprobacion: number;
}
export interface CountByPair {
  primeraEtiqueta: string;
  segundaEtiqueta: string;
  cantidad: number;
}
export interface ResultsByYear {
  anioIngreso: number;
  aprobadas: number;
  noAprobadas: number;
  evaluaciones: number;
  porcentajeAprobacion: number;
}
export interface DashboardData {
  resumen: {
    evaluaciones: number;
    aspirantesUnicos: number;
    aprobadas: number;
    porcentajeAprobacion: number;
  };
  aspirantesPorInstitucion: CountByLabel[];
  aprobadasPorMateria: CountByLabel[];
  aprobacionPorMateria: RateByLabel[];
  aspirantesPorSexoInstitucion: CountByPair[];
  aprobacionPorInstitucion: RateByLabel[];
  resultadosPorAnio: ResultsByYear[];
  aprobadosPorCarreraAnio: CareerYearCount[];
  edadPorCarrera: AverageByLabel[];
  resumenCarreras: CareerSummary[];
  edadAprobadosPorCarreraInstitucion: AverageByPair[];
  aprobadosPorMunicipioCarrera: CountByTriple[];
  evaluacionesPublicasPorMesMateria: CountByPair[];
  topCarrerasJuveniles: CountByLabel[];
  aprobacionPorEdad: AgeRate[];
  oportunidadesPorMateria: AverageOpportunities[];
  noAprobaronPrimerIntentoPorCarrera: CountByLabel[];
  topMunicipios: CountByPair[];
  carrerasMasDemandadasPorDepartamento: CountByPair[];
}
export interface CareerYearCount {
  carrera: string;
  anioIngreso: number;
  aspirantes: number;
}
export interface AverageByLabel {
  etiqueta: string;
  promedio: number;
  conDato: number;
  sinDato: number;
}
export interface AverageByPair {
  primeraEtiqueta: string;
  segundaEtiqueta: string;
  promedio: number;
  conDato: number;
  sinDato: number;
}
export interface CountByTriple {
  primeraEtiqueta: string;
  segundaEtiqueta: string;
  terceraEtiqueta: string;
  cantidad: number;
}
export interface AgeRate {
  edad: number;
  aprobadas: number;
  evaluaciones: number;
  porcentajeAprobacion: number;
}
export interface AverageOpportunities {
  materia: string;
  promedio: number;
  grupos: number;
}
export interface CareerSummary {
  anioIngreso: number;
  carrera: string;
  evaluaciones: number;
  aspirantesDistintos: number;
  aprobadas: number;
  noAprobadas: number;
  porcentajeAprobacion: number;
  sinEdad: number;
}
export interface ChartDatum {
  label: string;
  value: number;
}
export type ChartKind = 'bar' | 'donut';
export interface DashboardQuery {
  mongoId: string;
  id: string;
  label: string;
  title: string;
  hint: string;
  chart: ChartKind;
  chartData: (data: DashboardData) => ChartDatum[];
}
