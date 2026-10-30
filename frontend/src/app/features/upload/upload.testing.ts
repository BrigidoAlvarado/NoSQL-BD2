import { UploadFileResult, UploadProgress } from './upload.models';

export const completedFile: UploadFileResult = {
  nombreArchivo: 'uno.csv', sha256Archivo: 'hash', estado: 'COMPLETA',
  registrosLeidos: 1000, registrosProcesados: 1000, registrosRechazados: 0,
  duracionMilisegundos: 5000, reutilizada: false, detalle: 'Carga sincronizada.', rechazos: [],
};

export const idleProgress: UploadProgress = {
  id: null, activa: false, estado: 'INACTIVA', totalArchivos: 0, archivoActual: 0,
  nombreArchivo: null, etapa: null, procesadas: 0, totalEvaluaciones: 0,
  eventos: [], archivos: [],
};

export const activeProgress: UploadProgress = {
  ...idleProgress,
  id: 'lote-prueba', activa: true, estado: 'EN_CURSO', totalArchivos: 2, archivoActual: 1,
  nombreArchivo: 'uno.csv', etapa: 'NEO4J', procesadas: 500, totalEvaluaciones: 1000,
  eventos: [{ secuencia: 1, fecha: '2026-10-30T12:00:00Z', mensaje: 'Neo4j: 500 de 1000 evaluaciones procesadas.' }],
};
