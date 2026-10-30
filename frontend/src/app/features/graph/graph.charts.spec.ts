import { GRAPH_CHARTS, graphChartRows } from './graph.charts';
import { QueryData } from './graph.models';

describe('Datos de gráficas Cypher', () => {
  it('define categorías y métricas explícitas para las diez consultas', () => {
    expect(Object.keys(GRAPH_CHARTS)).toEqual(Array.from({length: 10}, (_, index) => `G${String(index + 1).padStart(2, '0')}`));
    for (const [id, definition] of Object.entries(GRAPH_CHARTS)) {
      const row = Object.fromEntries(definition.categories.map(key => [key, key === 'anioIngreso' ? 2024 : 'Categoría']));
      for (const metric of definition.metrics) row[metric] = 12;
      const result: QueryData = {id, titulo: id, descripcion: '', columnas: Object.keys(row), filas: [row]};
      for (const metric of definition.metrics) {
        expect(graphChartRows(result, metric)).toEqual([{label: definition.categories.map(key => String(row[key])).join(' · '), value: 12}]);
      }
    }
  });
  it('separa personas y oportunidades en G07 y conserva decimales', () => {
    const result: QueryData = {id: 'G07', titulo: '', descripcion: '', columnas: [], filas: [
      {anioIngreso: 2024, materia: 'MATEMATICA', aspirantesReintentaron: 30, promedioOportunidades: 2.35},
    ]};
    expect(graphChartRows(result, 'aspirantesReintentaron')[0].value).toBe(30);
    expect(graphChartRows(result, 'promedioOportunidades')[0].value).toBe(2.35);
  });
  it('ordena por valor, conserva ceros y descarta métricas ausentes o inválidas', () => {
    const result: QueryData = {id: 'G02', titulo: '', descripcion: '', columnas: [], filas: [
      {materia: 'MATEMATICA', resultado: 'NO APROBADO', evaluaciones: 0},
      {materia: 'COMPUTACION', resultado: 'APROBADO', evaluaciones: 8},
      {materia: 'FISICA', resultado: 'APROBADO', evaluaciones: null},
    ]};
    expect(graphChartRows(result, 'evaluaciones').map(row => row.value)).toEqual([8, 0]);
    expect(result.filas[0]['evaluaciones']).toBe(0);
    expect(graphChartRows({...result, filas: []}, 'evaluaciones')).toEqual([]);
  });
});
