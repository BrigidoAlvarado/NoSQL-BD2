import { CountByLabel, CountByPair, RateByLabel } from '../dashboard.models';

export const count = (rows: CountByLabel[]) =>
  rows.map((row) => ({ label: row.etiqueta, value: row.cantidad }));
export const rate = (rows: RateByLabel[]) =>
  rows.map((row) => ({ label: row.etiqueta, value: row.porcentajeAprobacion }));
export const pair = (rows: CountByPair[]) =>
  rows.map((row) => ({
    label: `${row.primeraEtiqueta} · ${row.segundaEtiqueta}`,
    value: row.cantidad,
  }));
