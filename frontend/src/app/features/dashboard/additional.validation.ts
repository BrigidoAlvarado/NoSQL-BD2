import { AnalysisRow } from './additional.models';

const FIELDS: Record<string, string[]> = {
  M21: ['total', 'aprobadas', 'noAprobadas', 'porcentaje'],
  M22: ['conFallo', 'recuperados', 'porcentaje'],
  M23: ['aspirantes', 'totalCohorte', 'porcentaje'],
  M24: ['incluidos', 'excluidos', 'promedioDias', 'minimoDias', 'maximoDias'],
  M25: ['total', 'sinEdad', 'carreraDesconocida', 'procedenciaDesconocida', 'porcentajeSinEdad', 'porcentajeCarreraDesconocida', 'porcentajeProcedenciaDesconocida'],
};
export function isAdditionalRows(value: unknown, id: string): value is AnalysisRow[] {
  if (!Array.isArray(value) || !FIELDS[id]) return false;
  return value.every(row => {
    if (!row || typeof row !== 'object' || !row._id || !Number.isInteger(row._id.anio)) return false;
    if (['M21', 'M22'].includes(id) && typeof row._id.materia !== 'string') return false;
    if (id === 'M21' && !Number.isInteger(row._id.oportunidad)) return false;
    if (id === 'M23' && !Number.isInteger(row._id.materias)) return false;
    return FIELDS[id].every(field => {
      const number = row[field];
      if (id === 'M24' && field.endsWith('Dias') && number === null) return true;
      return typeof number === 'number' && Number.isFinite(number) && number >= 0
        && (!field.startsWith('porcentaje') || number <= 100);
    });
  });
}
