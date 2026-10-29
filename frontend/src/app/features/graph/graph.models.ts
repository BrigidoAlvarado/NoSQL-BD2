export interface GraphNode {
  id: string;
  etiqueta: string;
  titulo: string;
  propiedades: Record<string, unknown>;
}
export interface GraphEdge {
  origen: string;
  destino: string;
  tipo: string;
}
export interface GraphData {
  nodos: GraphNode[];
  relaciones: GraphEdge[];
  evaluacionesIncluidas: number;
}
export interface QueryData {
  id: string;
  titulo: string;
  descripcion: string;
  columnas: string[];
  filas: Record<string, unknown>[];
}
export interface QueryState {
  loading: boolean;
  error: string | null;
  data: QueryData | null;
}
