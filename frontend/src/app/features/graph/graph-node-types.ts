/** Referencia única de nombres y colores para el lienzo y sus filtros. */
export const GRAPH_NODE_TYPES: Record<string, { label: string; color: string }> = {
  Aspirante: { label: 'Aspirante', color: '#266678' },
  Evaluacion: { label: 'Evaluación', color: '#7a4da0' },
  Carrera: { label: 'Carrera', color: '#bd6a2d' },
  Materia: { label: 'Materia / curso', color: '#28765a' },
  Resultado: { label: 'Resultado', color: '#b63d65' },
  ProcedenciaEducativa: { label: 'Procedencia educativa', color: '#816039' },
  Municipio: { label: 'Municipio', color: '#337dcc' },
  Departamento: { label: 'Departamento', color: '#9a8615' },
};

export function graphNodeType(type: string): { label: string; color: string } {
  return GRAPH_NODE_TYPES[type] ?? { label: type, color: '#626d78' };
}
