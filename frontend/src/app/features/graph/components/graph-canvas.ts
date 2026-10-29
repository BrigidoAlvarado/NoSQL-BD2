import { Component, ElementRef, effect, input, viewChild, OnDestroy, signal, untracked } from '@angular/core';
import { DataSet, Network } from 'vis-network/standalone';
import { GraphData, GraphNode } from '../graph.models';
import { displayCareerName } from '../../../shared/career-name.pipe';
import { graphNodeType } from '../graph-node-types';

/** Dibuja la muestra y permite navegar y seleccionar sus relaciones. */
@Component({
  selector: 'app-graph-canvas',
  templateUrl: './graph-canvas.html',
  styleUrl: './graph-canvas.css',
})
export class GraphCanvas implements OnDestroy {
  readonly data = input.required<GraphData>();
  readonly canvas = viewChild<ElementRef<HTMLDivElement>>('canvas');
  readonly selected = signal<GraphNode | null>(null);
  readonly nodeType = graphNodeType;
  network?: Network;
  private nodes = new DataSet<any>();
  private edges = new DataSet<any>();
  private zoom = 1;
  readonly hiddenTypes = signal<string[]>([]);
  types(): string[] { return [...new Set(this.data().nodos.map(node => node.etiqueta))]; }
  toggle(type: string): void {
    this.hiddenTypes.update(types => types.includes(type) ? types.filter(item => item !== type) : [...types, type]);
    this.clear();
  }
  visibleCount(): number { return this.data().nodos.filter(node => !this.hiddenTypes().includes(node.etiqueta)).length; }
  constructor() {
    effect(() => {
      const container = this.canvas()?.nativeElement;
      const data = this.data();
      if (!container) return;
      this.network?.destroy();
      this.selected.set(null);
      this.nodes = new DataSet(data.nodos.map(node => ({ id: node.id })));
      this.edges = new DataSet(data.relaciones.map((edge, id) => ({ id, from: edge.origen, to: edge.destino, arrows: 'to' })));
      this.zoom = 1;
      untracked(() => this.updateAppearance());
      this.network = new Network(container, { nodes: this.nodes, edges: this.edges      }, { nodes: { shape: 'dot', size: 13, font: { size: 12 } },
        physics: { stabilization: { iterations: 150 }, barnesHut: { gravitationalConstant: -5000, springLength: 140 } },
        interaction: { hover: true, navigationButtons: false, keyboard: true },
      });
      this.network.on('selectNode', params => this.select(String(params.nodes[0])));
      this.network.on('deselectNode', () => this.clear());
      this.network.on('zoom', params => { this.zoom = params.scale; this.updateAppearance(); });
    });
  }
  select(id: string): void {
    const node = this.data().nodos.find(node => node.id === id) ?? null;
    this.selected.set(node);
    this.updateAppearance();
    if (node) {
      this.network?.selectNodes([id], true);
      this.network?.focus(id, { scale: 1.2, animation: true });
    } else this.clear();
  }
  clear(): void { this.selected.set(null); this.network?.unselectAll(); this.updateAppearance(); }
  private updateAppearance(): void {
    const selected = this.selected()?.id;
    const neighbors = new Set([selected]);
    for (const edge of this.data().relaciones) {
      if (edge.origen === selected) neighbors.add(edge.destino);
      if (edge.destino === selected) neighbors.add(edge.origen);
    }
    const hidden = new Set(this.data().nodos.filter(node => this.hiddenTypes().includes(node.etiqueta)).map(node => node.id));
    this.nodes.update(this.data().nodos.map(node => {
      const active = !selected || neighbors.has(node.id);
      const show = this.zoom > 1.5 || neighbors.has(node.id) || ['Carrera', 'Municipio', 'Departamento'].includes(node.etiqueta);
      const color = active ? graphNodeType(node.etiqueta).color : '#dddddd';
      return { id: node.id, hidden: hidden.has(node.id), label: show ? displayCareerName(node.titulo) : ' ',
        color: { background: color, border: color, highlight: { background: color, border: '#172b45' }, hover: { background: color, border: '#172b45' } },
        font: { color: active ? '#222222' : '#999999' }, borderWidth: node.id === selected ? 4 : 1 };
    }));
    this.edges.update(this.data().relaciones.map((edge, id) => ({ id, hidden: hidden.has(edge.origen) || hidden.has(edge.destino),
      color: !selected || edge.origen === selected || edge.destino === selected ? '#657786' : '#eeeeee',
      width: edge.origen === selected || edge.destino === selected ? 3 : 1,
    })));
  }
  properties(node: GraphNode): [string, string][] {
    return Object.entries(node.propiedades).map(([key, value]) => [key, String(value)]);
  }
  ngOnDestroy(): void { this.network?.destroy(); }
}
