import { Component, ElementRef, effect, input, viewChild, OnDestroy, signal } from '@angular/core';
import { Network } from 'vis-network/standalone';
import { GraphData, GraphNode } from '../graph.models';
import { displayCareerName } from '../../../shared/career-name.pipe';

/** Dibuja la muestra y permite navegar y seleccionar sus relaciones. */
@Component({
  selector: 'app-graph-canvas',
  template: `
    <p>Carreras: naranja · Aspirantes: azul · Evaluaciones: violeta · Ubicación: verde</p>
    <button (click)="network?.fit()">Ajustar vista</button>
    <button (click)="clear()">Quitar selección</button>
    <div #canvas class="graph-canvas" style="height:560px;width:100%;border:1px solid #bbb" aria-label="Grafo interactivo de la muestra"></div>
    <label>Seleccionar entidad <select (change)="select($any($event.target).value)">
      <option value="">Sin selección</option>
      @for (node of data().nodos; track node.id) { <option [value]="node.id">{{ node.etiqueta }} · {{ node.titulo }}</option> }
    </select></label>
    @if (selected(); as node) {
      <aside><h3>{{ node.etiqueta }} · {{ node.titulo }}</h3><p>{{ node.id }}</p>
        <dl>@for (entry of properties(node); track entry[0]) { <dt>{{ entry[0] }}</dt><dd>{{ entry[1] }}</dd> }</dl>
      </aside>
    }
  `,
})
export class GraphCanvas implements OnDestroy {
  readonly data = input.required<GraphData>();
  readonly canvas = viewChild<ElementRef<HTMLDivElement>>('canvas');
  readonly selected = signal<GraphNode | null>(null);
  network?: Network;
  constructor() {
    effect(() => {
      const container = this.canvas()?.nativeElement;
      const data = this.data();
      if (!container) return;
      this.network?.destroy();
      this.selected.set(null);
      const colors: Record<string, string> = { Carrera: '#bd6a2d', Aspirante: '#266678', Evaluacion: '#7a4da0' };
      this.network = new Network(container, {
        nodes: data.nodos.map(node => ({ id: node.id,
          label: ['Carrera', 'Municipio', 'Departamento'].includes(node.etiqueta) ? displayCareerName(node.titulo) : ' ',
          color: colors[node.etiqueta] ?? '#28765a',
        })),
        edges: data.relaciones.map(edge => ({ from: edge.origen, to: edge.destino, arrows: 'to', label: '', title: edge.tipo })),
      }, { nodes: { shape: 'dot', size: 13, font: { size: 12 } },
        physics: { stabilization: { iterations: 150 }, barnesHut: { gravitationalConstant: -5000, springLength: 140 } },
        interaction: { hover: true, navigationButtons: false, keyboard: true },
      });
      this.network.on('selectNode', params => this.select(String(params.nodes[0])));
      this.network.on('deselectNode', () => this.selected.set(null));
    });
  }
  select(id: string): void {
    const node = this.data().nodos.find(node => node.id === id) ?? null;
    this.selected.set(node);
    if (node) {
      this.network?.selectNodes([id], true);
      this.network?.focus(id, { scale: 1.2, animation: true });
    } else this.clear();
  }
  clear(): void { this.selected.set(null); this.network?.unselectAll(); }
  properties(node: GraphNode): [string, string][] {
    return Object.entries(node.propiedades).map(([key, value]) => [key, String(value)]);
  }
  ngOnDestroy(): void { this.network?.destroy(); }
}
