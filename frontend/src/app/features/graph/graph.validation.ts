import { GraphData, QueryData } from './graph.models';

export function isGraph(value: unknown): value is GraphData {
  return (
    typeof value === 'object' &&
    value !== null &&
    Array.isArray((value as GraphData).nodos) &&
    Array.isArray((value as GraphData).relaciones) &&
    typeof (value as GraphData).evaluacionesIncluidas === 'number'
  );
}
export function isQuery(value: unknown): value is QueryData {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as QueryData).id === 'string' &&
    Array.isArray((value as QueryData).columnas) &&
    Array.isArray((value as QueryData).filas)
  );
}
