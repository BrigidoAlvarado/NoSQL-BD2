export interface RejectedRow {
  line: number;
  code: string;
  detail: string;
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
