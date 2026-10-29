import { vi } from 'vitest';
vi.mock('vis-network/standalone', () => import('./graph-network.mock'));
import { TestBed } from '@angular/core/testing';
import { GraphCanvas } from './graph-canvas';
import { GRAPH_NODE_TYPES } from '../graph-node-types';
import { GraphData } from '../graph.models';

import { captured } from './graph-network.mock';

/** Comprueba el filtro sobre los datos entregados al motor visual. */
describe('Colores y filtros de entidades del grafo', () => {
  const types = Object.keys(GRAPH_NODE_TYPES);
  const data: GraphData = {
    evaluacionesIncluidas: 1,
    nodos: types.map(type => ({ id: type, etiqueta: type, titulo: type, propiedades: {} })),
    relaciones: [
      { origen: 'Evaluacion', destino: 'Materia', tipo: 'EVALUA' },
      { origen: 'Evaluacion', destino: 'Carrera', tipo: 'PARA_CARRERA' },
    ],
  };
  it('usa ocho colores distintos y los mismos indicadores en filtros y nodos', async () => {
    const fixture = TestBed.createComponent(GraphCanvas);
    fixture.componentRef.setInput('data', data);
    await fixture.whenStable();
    const swatches = fixture.nativeElement.querySelectorAll('.type-color');
    expect(swatches).toHaveLength(8);
    expect(new Set(types.map(type => GRAPH_NODE_TYPES[type].color)).size).toBe(8);
    for (const type of types) {
      expect(captured.data.nodes.items.get(type).color.background).toBe(GRAPH_NODE_TYPES[type].color);
    }
    expect(fixture.nativeElement.textContent).toContain('Materia / curso');
  });
  it('oculta solo la materia y sus enlaces sin cambiar colores ni nodos de otros tipos', async () => {
    const fixture = TestBed.createComponent(GraphCanvas);
    fixture.componentRef.setInput('data', data);
    await fixture.whenStable();
    fixture.componentInstance.toggle('Materia');
    expect(captured.data.nodes.items.get('Materia').hidden).toBe(true);
    expect(captured.data.edges.items.get(0).hidden).toBe(true);
    expect(captured.data.edges.items.get(1).hidden).toBe(false);
    expect(fixture.componentInstance.visibleCount()).toBe(7);
    for (const type of types.filter(type => type !== 'Materia')) {
      expect(captured.data.nodes.items.get(type).hidden).toBe(false);
      expect(captured.data.nodes.items.get(type).color.background).toBe(GRAPH_NODE_TYPES[type].color);
    }
    fixture.componentInstance.toggle('Materia');
    expect(fixture.componentInstance.visibleCount()).toBe(8);
    expect(captured.data.edges.items.get(0).hidden).toBe(false);
  });
});
