import { HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

interface GraphNode { id: string; etiqueta: string; titulo: string; propiedades: Record<string, unknown>; }
interface GraphEdge { origen: string; destino: string; tipo: string; }
interface GraphData { nodos: GraphNode[]; relaciones: GraphEdge[]; evaluacionesIncluidas: number; }
interface QueryData { id: string; titulo: string; descripcion: string; columnas: string[]; filas: Record<string, unknown>[]; }

@Component({
  selector: 'app-graph',
  template: `
    <p class="eyebrow">RELACIONES ENTRE DATOS</p><h1>Explorador de grafos</h1>
    <p class="page-description">Recorre evaluaciones, aspirantes, carreras, materias y procedencia desde Neo4j.</p>
    <section class="card graph-filters"><h2>Filtros del grafo</h2><div class="filter-grid">
      <label>Año de ingreso<input type="number" [value]="anio" (input)="anio=inputValue($event)" placeholder="Todos"></label>
      <label>Carrera<input [value]="carrera" (input)="carrera=inputValue($event)" placeholder="Texto exacto"></label>
      <label>Departamento<input [value]="departamento" (input)="departamento=inputValue($event)" placeholder="Texto exacto"></label>
      <label>Resultado<select [value]="resultado" (change)="resultado=inputValue($event)"><option value="">Todos</option><option>APROBADO</option><option>NO APROBADO</option></select></label>
    </div><button class="button" type="button" (click)="loadGraph()">Aplicar filtros</button><button class="button secondary" type="button" (click)="clearFilters()">Limpiar</button>
    </section>
    @if (error()) { <p class="error-message" role="alert">{{error()}}</p> }
    @if (loading()) { <p class="processing" role="status">Consultando Neo4j…</p> }
    @if (graph(); as data) {
      <section class="card graph-card"><div class="result-heading"><h2>Grafo de evaluaciones</h2><span class="tag">{{data.evaluacionesIncluidas}} evaluaciones · {{data.nodos.length}} nodos</span></div>
      @if (!data.nodos.length) { <p class="empty-result">No hay nodos con esos filtros. Prueba otro año o elimina los filtros.</p> } @else {
        <div class="graph-canvas" role="img" aria-label="Grafo de relaciones de evaluaciones">
          <svg viewBox="0 0 900 560" preserveAspectRatio="xMidYMid meet">
            @for (edge of data.relaciones; track edge.origen + edge.destino + edge.tipo) { <line class="graph-edge" [attr.x1]="point(edge.origen).x" [attr.y1]="point(edge.origen).y" [attr.x2]="point(edge.destino).x" [attr.y2]="point(edge.destino).y" /> }
            @for (node of data.nodos; track node.id) { <g class="graph-node" [class]="'graph-node type-' + node.etiqueta" [attr.transform]="'translate(' + point(node.id).x + ',' + point(node.id).y + ')'" tabindex="0"><title>{{node.etiqueta}}: {{node.titulo}}</title><circle r="13"/><text y="29">{{short(node.titulo)}}</text></g> }
          </svg>
        </div>
        <p class="hint">Cada punto representa una entidad; las líneas siguen relaciones reales de Neo4j. El límite es 40 evaluaciones para mantener el grafo legible.</p>
      }
      </section>
    }
    <section class="card graph-queries"><h2>Consultas Cypher</h2><p class="hint">Selecciona una consulta para ver su resultado tabular.</p>
      <div class="query-actions"><select [value]="queryId" (change)="queryId=inputValue($event)"><option value="G01">G01 · Demanda por carrera</option><option value="G02">G02 · Resultados por materia</option><option value="G03">G03 · Aprobados por departamento</option><option value="G04">G04 · Cambios de carrera</option><option value="G05">G05 · Cohortes, materias y resultado</option></select><button class="button" type="button" (click)="loadQuery()">Ejecutar consulta</button></div>
      @if (query(); as result) { <h3>{{result.id}} · {{result.titulo}}</h3><p class="hint">{{result.descripcion}}</p>@if (!result.filas.length) { <p class="empty-result">La consulta no encontró resultados.</p> } @else {<table><thead><tr>@for(c of result.columnas;track c){<th>{{c}}</th>}</tr></thead><tbody>@for(row of result.filas;track $index){<tr>@for(c of result.columnas;track c){<td>{{value(row,c)}}</td>}</tr>}</tbody></table>} }
    </section>
  `,
})
export class GraphExplorer {
  private readonly api = inject(ApiService);
  readonly graph = signal<GraphData | null>(null); readonly query = signal<QueryData | null>(null);
  readonly loading = signal(false); readonly error = signal<string | null>(null);
  anio = ''; carrera = ''; departamento = ''; resultado = ''; queryId = 'G01';
  private positions = new Map<string, {x:number;y:number}>();
  constructor() { this.loadGraph(); this.loadQuery(); }
  loadGraph(): void {
    this.loading.set(true); this.error.set(null); let params = new HttpParams().set('limite','40');
    if (this.anio) params=params.set('anio',this.anio); if(this.carrera.trim())params=params.set('carrera',this.carrera.trim()); if(this.departamento.trim())params=params.set('departamento',this.departamento.trim()); if(this.resultado)params=params.set('resultado',this.resultado);
    this.api.get<GraphData>('grafos',params).subscribe({next:data=>{this.graph.set(data);this.positions.clear();data.nodos.forEach((n,i)=>this.positions.set(n.id,this.layout(i,data.nodos.length)));this.loading.set(false);},error:e=>{this.error.set(e instanceof ApiError?e.message:'No se pudo consultar Neo4j.');this.loading.set(false);}});
  }
  loadQuery(): void { this.api.get<QueryData>(`grafos/consultas/${this.queryId}`).subscribe({next:q=>this.query.set(q),error:e=>this.error.set(e instanceof ApiError?e.message:'No se pudo ejecutar la consulta.')}); }
  clearFilters(): void { this.anio='';this.carrera='';this.departamento='';this.resultado='';this.loadGraph(); }
  point(id:string): {x:number;y:number} { return this.positions.get(id) ?? {x:450,y:280}; }
  short(value:string):string { return value.length>18?`${value.slice(0,17)}…`:value; }
  value(row:Record<string,unknown>,column:string):string { return String(row[column] ?? ''); }
  inputValue(event: Event): string { return (event.target as HTMLInputElement).value; }
  private layout(index:number,total:number):{x:number;y:number}{const angle=(Math.PI*2*index)/Math.max(total,1)-Math.PI/2;const radius=Math.min(220,80+total*2);return{x:450+Math.cos(angle)*radius,y:280+Math.sin(angle)*radius};}
}
