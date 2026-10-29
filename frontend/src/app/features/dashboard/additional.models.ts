export interface AnalysisGroup {
  anio: number;
  materia?: string;
  oportunidad?: number;
  materias?: number;
}
export interface AdditionalRow extends Record<string, unknown> {
  _id: AnalysisGroup;
}
export interface OpportunityRow extends AdditionalRow {
  total: number; aprobadas: number; noAprobadas: number; porcentaje: number;
}
export interface RecoveryRow extends AdditionalRow {
  conFallo: number; recuperados: number; porcentaje: number;
}
export interface SubjectsRow extends AdditionalRow {
  aspirantes: number; totalCohorte: number; porcentaje: number;
}
export interface IntervalRow extends AdditionalRow {
  incluidos: number; excluidos: number; promedioDias: number | null;
  minimoDias: number | null; maximoDias: number | null;
}
export interface QualityRow extends AdditionalRow {
  total: number; sinEdad: number; carreraDesconocida: number; procedenciaDesconocida: number;
  porcentajeSinEdad: number; porcentajeCarreraDesconocida: number; porcentajeProcedenciaDesconocida: number;
}
export type AnalysisRow = OpportunityRow | RecoveryRow | SubjectsRow | IntervalRow | QualityRow;
