export interface EvaluationResult {
  aspiranteId: string;
  fechaAsignacion: string;
  materia: string;
  oportunidad: number;
  anioIngreso: number;
  aprobacion: string;
  carreraObjetivo: string;
  tipoInstitucion: string;
  municipio: string;
  departamento: string;
}
export interface SearchData {
  criterio: string;
  termino: string;
  totalDevuelto: number;
  resultados: EvaluationResult[];
}
export interface ApplicantEvaluation extends EvaluationResult {
  sexo: string;
  anioNacimiento: number | null;
  edadIngreso: number | null;
}
export interface ApplicantDetail {
  aspiranteId: string;
  totalEvaluaciones: number;
  evaluacionesAprobadas: number;
  evaluaciones: ApplicantEvaluation[];
}
