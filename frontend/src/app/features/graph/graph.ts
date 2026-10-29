import { GraphCanvas } from './components/graph-canvas';
import { QUERY_TITLES, columnLabel } from './graph.presentation';
import { isGraph, isQuery } from './graph.validation';
import { HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { GRAPH_SECTIONS } from './graph.sections';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

import { GraphData, QueryData, QueryState } from './graph.models';

@Component({
  selector: 'app-graph',
  imports: [GraphCanvas],
  templateUrl: './graph.html',
})
export class GraphExplorer {
  private readonly api = inject(ApiService);
  readonly graph = signal<GraphData | null>(null);
  readonly sections = GRAPH_SECTIONS;
  readonly queryTitles = QUERY_TITLES;
  readonly columnLabel = columnLabel;
  readonly section = signal(this.sections[0]);
  readonly queryStates = signal<Record<string, QueryState>>({});
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  anio = '';
  carrera = '';
  departamento = '';
  resultado = '';
  constructor() {
    inject(ActivatedRoute)
      .queryParamMap.pipe(takeUntilDestroyed())
      .subscribe((params) => {
        const section =
          this.sections.find((section) => section.id === params.get('seccion')) ?? this.sections[0];
        this.section.set(section);
        if (section.id === 'grafo') {
          if (!this.graph() && !this.loading() && !this.error()) this.loadGraph();
        } else {
          section.queries.forEach((id) => {
            if (!this.queryStates()[id]) this.loadQuery(id);
          });
        }
      });
  }
  loadGraph(): void {
    this.loading.set(true);
    this.error.set(null);
    let params = new HttpParams().set('limite', '40');
    if (this.anio) params = params.set('anio', this.anio);
    if (this.carrera.trim()) params = params.set('carrera', this.carrera.trim());
    if (this.departamento.trim()) params = params.set('departamento', this.departamento.trim());
    if (this.resultado) params = params.set('resultado', this.resultado);
    this.api.get<GraphData>('grafos', params).subscribe({
      next: (data) => {
        if (!isGraph(data)) {
          this.error.set('El servidor devolvió un grafo inválido. Ajusta los filtros o reintenta.');
          this.loading.set(false);
          return;
        }
        this.graph.set(data);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(e instanceof ApiError ? e.message : 'No se pudo consultar Neo4j.');
        this.loading.set(false);
      },
    });
  }
  loadQuery(id: string): void {
    if (!this.queryTitles[id] || this.queryStates()[id]?.loading) return;
    this.setQueryState(id, { loading: true, error: null, data: null });
    this.api.get<QueryData>(`grafos/consultas/${id}`).subscribe({
      next: (data) =>
        this.setQueryState(
          id,
          isQuery(data) && data.id === id
            ? { loading: false, error: null, data }
            : {
                loading: false,
                error: 'El servidor devolvió un resultado inválido. Puedes reintentar.',
                data: null,
              },
        ),
      error: (error) =>
        this.setQueryState(id, {
          loading: false,
          error:
            error instanceof ApiError ? error.message : 'No se pudo ejecutar la consulta. Puedes reintentar.',
          data: null,
        }),
    });
  }
  private setQueryState(id: string, state: QueryState): void {
    this.queryStates.update((states) => ({ ...states, [id]: state }));
  }
  clearFilters(): void {
    this.anio = '';
    this.carrera = '';
    this.departamento = '';
    this.resultado = '';
    this.loadGraph();
  }
  value(row: Record<string, unknown>, column: string): string {
    return String(row[column] ?? '');
  }
  inputValue(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }
}
