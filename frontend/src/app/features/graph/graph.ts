import { HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { GRAPH_SECTIONS } from './graph.sections';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

interface GraphNode { id: string; etiqueta: string; titulo: string; propiedades: Record<string, unknown>; }
interface GraphEdge { origen: string; destino: string; tipo: string; }
interface GraphData { nodos: GraphNode[]; relaciones: GraphEdge[]; evaluacionesIncluidas: number; }
interface QueryData { id: string; titulo: string; descripcion: string; columnas: string[]; filas: Record<string, unknown>[]; }
interface QueryState { loading: boolean; error: string | null; data: QueryData | null; }

@Component({
  selector: 'app-graph',
  templateUrl: './graph.html',
})
export class GraphExplorer {
  private readonly api = inject(ApiService);
  readonly graph = signal<GraphData | null>(null);
  readonly sections = GRAPH_SECTIONS;
  readonly section = signal(this.sections[0]);
  readonly queryStates = signal<Record<string, QueryState>>({});
  readonly queryTitles: Record<string, string> = {G01: 'Demanda por carrera', G02: 'Resultados por materia', G03: 'Aprobados por departamento y carrera', G04: 'Aspirantes vinculados con distintas carreras', G05: 'Resultados por cohorte, carrera y materia', G06: 'Resultados por tipo de institución', G07: 'Reintentos por materia y cohorte', G08: 'Demanda por municipio y carrera', G09: 'Resultados por sexo, carrera y materia', G10: 'Aspirantes con resultados mixtos'};
  readonly loading = signal(false); readonly error = signal<string | null>(null);
  anio = ''; carrera = ''; departamento = ''; resultado = '';
  readonly nodeTypes = [{label:'Aspirante',color:'#266678'}, {label:'Evaluación',color:'#7a4da0'}, {label:'Carrera',color:'#bd6a2d'}, {label:'Materia',color:'#28765a'}, {label:'Resultado',color:'#9b345d'}, {label:'Procedencia y ubicación',color:'#28649e'}];
  private positions = new Map<string, {x:number;y:number}>();
  constructor() {
    inject(ActivatedRoute).queryParamMap.pipe(takeUntilDestroyed()).subscribe(params => {
      const section = this.sections.find(section => section.id === params.get('seccion')) ?? this.sections[0];
      this.section.set(section);
      if (section.id === 'grafo') {
        if (!this.graph() && !this.loading() && !this.error()) this.loadGraph();
      } else {
        section.queries.forEach(id => { if (!this.queryStates()[id]) this.loadQuery(id); });
      }
    });
  }
  loadGraph(): void {
    this.loading.set(true); this.error.set(null); let params = new HttpParams().set('limite','40');
    if (this.anio) params=params.set('anio',this.anio); if(this.carrera.trim())params=params.set('carrera',this.carrera.trim()); if(this.departamento.trim())params=params.set('departamento',this.departamento.trim()); if(this.resultado)params=params.set('resultado',this.resultado);
    this.api.get<GraphData>('grafos',params).subscribe({next:data=>{if(!isGraph(data)){this.error.set('El servidor devolvió un grafo inválido. Ajusta los filtros o reintenta.');this.loading.set(false);return;}this.graph.set(data);this.positions.clear();data.nodos.forEach((n,i)=>this.positions.set(n.id,this.layout(i,data.nodos.length)));this.loading.set(false);},error:e=>{this.error.set(e instanceof ApiError?e.message:'No se pudo consultar Neo4j.');this.loading.set(false);}});
  }
  loadQuery(id: string): void {
    if (!this.queryTitles[id] || this.queryStates()[id]?.loading) return;
    this.setQueryState(id, {loading: true, error: null, data: null});
    this.api.get<QueryData>(`grafos/consultas/${id}`).subscribe({
      next: data => this.setQueryState(id, isQuery(data) && data.id === id
        ? {loading: false, error: null, data}
        : {loading: false, error: 'El servidor devolvió un resultado inválido. Puedes reintentar.', data: null}),
      error: error => this.setQueryState(id, {loading: false, error: error instanceof ApiError ? error.message : 'No se pudo ejecutar la consulta. Puedes reintentar.', data: null}),
    });
  }
  private setQueryState(id: string, state: QueryState): void { this.queryStates.update(states => ({...states, [id]: state})); }
  columnLabel(column: string): string { return ({carrera:'Carrera', evaluaciones:'Evaluaciones', materia:'Materia', resultado:'Resultado', departamento:'Departamento', municipio:'Municipio', tipoInstitucion:'Tipo de institución', sexo:'Sexo', aspirantes:'Aspirantes', aspirantesReintentaron:'Aspirantes con reintento', promedioOportunidades:'Promedio de oportunidades', carreraOrigen:'Carrera A', carreraDestino:'Carrera B', anioIngreso:'Año de ingreso'} as Record<string, string>)[column] ?? column; }
  clearFilters(): void { this.anio='';this.carrera='';this.departamento='';this.resultado='';this.loadGraph(); }
  point(id:string): {x:number;y:number} { return this.positions.get(id) ?? {x:450,y:280}; }
  short(value:string):string { return value.length>18?`${value.slice(0,17)}…`:value; }
  value(row:Record<string,unknown>,column:string):string { return String(row[column] ?? ''); }
  inputValue(event: Event): string { return (event.target as HTMLInputElement).value; }
  private layout(index:number,total:number):{x:number;y:number}{const angle=(Math.PI*2*index)/Math.max(total,1)-Math.PI/2;const radius=Math.min(220,80+total*2);return{x:450+Math.cos(angle)*radius,y:280+Math.sin(angle)*radius};}
}

function isGraph(value: unknown): value is GraphData { return typeof value === 'object' && value !== null && Array.isArray((value as GraphData).nodos) && Array.isArray((value as GraphData).relaciones) && typeof (value as GraphData).evaluacionesIncluidas === 'number'; }
function isQuery(value: unknown): value is QueryData { return typeof value === 'object' && value !== null && typeof (value as QueryData).id === 'string' && Array.isArray((value as QueryData).columnas) && Array.isArray((value as QueryData).filas); }
