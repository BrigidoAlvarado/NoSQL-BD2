import { DashboardQuery } from './dashboard.models';
import { PANORAMA_QUERIES } from './queries/panorama.queries';
import { APROBACION_QUERIES } from './queries/aprobacion.queries';
import { CARRERAS_QUERIES } from './queries/carreras.queries';
import { PERFIL_QUERIES } from './queries/perfil.queries';
import { PROCEDENCIA_QUERIES } from './queries/procedencia.queries';

const QUERY_CATALOG: Record<string, readonly DashboardQuery[]> = {
  panorama: PANORAMA_QUERIES,
  aprobacion: APROBACION_QUERIES,
  carreras: CARRERAS_QUERIES,
  perfil: PERFIL_QUERIES,
  procedencia: PROCEDENCIA_QUERIES,
};

export const queries = (section: string): readonly DashboardQuery[] =>
  QUERY_CATALOG[section] ?? QUERY_CATALOG['panorama'];
