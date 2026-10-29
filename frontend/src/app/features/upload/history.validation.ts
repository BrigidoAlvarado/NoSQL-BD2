import { HistoryPage } from './upload.models';

export function isHistoryPage(value: unknown, path: string): value is HistoryPage {
  if (!value || typeof value !== 'object') return false;
  const page = value as Record<string, unknown>;
  if (!Array.isArray(page['items']) || !Number.isInteger(page['total']) || (page['total'] as number) < 0
      || !Number.isInteger(page['page']) || (page['page'] as number) < 0
      || !Number.isInteger(page['size']) || (page['size'] as number) < 1) return false;
  return page['items'].length <= (page['size'] as number) && page['items'].every(row => {
    if (!row || typeof row !== 'object') return false;
    if (path === 'cargas') return ['sha256Archivo', 'nombreArchivo', 'estado'].every(key => typeof row[key] === 'string');
    if (path.endsWith('/rechazos')) return Number.isInteger(row.fila) && typeof row.codigo === 'string'
      && typeof row.detalle === 'string' && !!row.valores && typeof row.valores === 'object';
    return ['aspiranteId', 'fechaAsignacion', 'materia', 'aprobacion', 'carreraObjetivo'].every(key => typeof row[key] === 'string')
      && Number.isInteger(row.oportunidad) && Number.isInteger(row.anioIngreso)
      && !!row.procedencia && Array.isArray(row.fuentes)
      && row.fuentes.every((source: Record<string, unknown>) => source && typeof source['sha256Archivo'] === 'string'
        && typeof source['nombreArchivo'] === 'string' && Number.isInteger(source['fila']));
  });
}
