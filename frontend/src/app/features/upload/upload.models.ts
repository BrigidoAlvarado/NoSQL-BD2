export interface RejectedRow {
  line: number;
  code: string;
  detail: string;
  values?: Record<string, string>;
}
export interface UploadFileResult {
  nombreArchivo: string;
  sha256Archivo: string | null;
  estado: string;
  registrosLeidos: number;
  registrosProcesados: number;
  registrosRechazados: number;
  duracionMilisegundos: number;
  reutilizada: boolean;
  detalle: string;
  rechazos: RejectedRow[];
}
export interface UploadResponse {
  archivos: UploadFileResult[];
  mensaje: string | null;
}

export interface UploadProgress {
  id: string | null;
  activa: boolean;
  estado: string;
  totalArchivos: number;
  archivoActual: number;
  nombreArchivo: string | null;
  etapa: string | null;
  procesadas: number;
  totalEvaluaciones: number;
  eventos: { secuencia: number; fecha: string; mensaje: string }[];
  archivos: UploadFileResult[];
}

export interface LoadRecord extends Record<string, unknown> {
  sha256Archivo: string;
  nombreArchivo: string;
  estado: string;
  iniciadaEn?: string | null;
  actualizadaEn?: string | null;
  filasLeidas?: number | null;
  filasValidas?: number | null;
  filasRechazadas?: number | null;
  mensaje?: string | null;
}
export interface HistoryRejection extends Record<string, unknown> {
  fila: number; codigo: string; detalle: string; valores: Record<string, string>;
}
export interface EvaluationSource {
  sha256Archivo: string; nombreArchivo: string; fila: number;
}
export interface HistoryEvaluation extends Record<string, unknown> {
  aspiranteId: string; fechaAsignacion: string; materia: string; oportunidad: number;
  anioIngreso: number; aprobacion: string; carreraObjetivo: string;
  anioNacimiento: number | null; edadIngreso: number | null;
  procedencia: Record<string, unknown>; fuentes: EvaluationSource[];
}
export interface HistoryPage {
  items: (LoadRecord | HistoryRejection | HistoryEvaluation)[];
  total: number; page: number; size: number;
}
