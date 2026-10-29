import { CareerNamePipe } from '../../../shared/career-name.pipe';
import { Component, computed, input } from '@angular/core';
import { GraphData } from '../graph.models';

@Component({
  imports: [CareerNamePipe],
  selector: 'app-graph-canvas',
  templateUrl: './graph-canvas.html',
})
export class GraphCanvas {
  readonly data = input.required<GraphData>();
  readonly nodeTypes = [
    { label: 'Aspirante', color: '#266678' },
    { label: 'Evaluación', color: '#7a4da0' },
    { label: 'Carrera', color: '#bd6a2d' },
    { label: 'Materia', color: '#28765a' },
    { label: 'Resultado', color: '#9b345d' },
    { label: 'Procedencia y ubicación', color: '#28649e' },
  ];

  private readonly positions = computed(
    () =>
      new Map(
        this.data().nodos.map((node, index) => [node.id, this.layout(index, this.data().nodos.length)]),
      ),
  );
  point(id: string): { x: number; y: number } {
    return this.positions().get(id) ?? { x: 450, y: 280 };
  }
  short(value: string): string {
    return value.length > 18 ? `${value.slice(0, 17)}…` : value;
  }

  private layout(index: number, total: number): { x: number; y: number } {
    const angle = (Math.PI * 2 * index) / Math.max(total, 1) - Math.PI / 2;
    const radius = Math.min(220, 80 + total * 2);
    return {
      x: 450 + Math.cos(angle) * radius,
      y: 280 + Math.sin(angle) * radius,
    };
  }
}
