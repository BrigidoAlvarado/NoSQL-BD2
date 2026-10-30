import { CategoryFilters, FilteredRows } from '../../shared/category-filters';
import { displayMunicipality } from '../../shared/municipality-name.pipe';
import { GraphCanvas } from './components/graph-canvas';
import { displayCareerName } from '../../shared/career-name.pipe';
import { QUERY_TITLES, columnLabel } from './graph.presentation';
import { isGraph, isQuery } from './graph.validation';
import { HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { GRAPH_SECTIONS } from './graph.sections';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

import { GraphData, QueryData, QueryState } from './graph.models';

@Component({
  selector: 'app-graph',
  imports: [GraphCanvas, CategoryFilters],
  templateUrl: './graph.html',
})
export class GraphExplorer {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly selectedQuery = signal<string | null>(null);
  readonly filteredRows = signal<FilteredRows | null>(null);
  filterFields(columns: string[]) {
    return columns.filter(column => !['aspirantes', 'evaluaciones', 'aprobados', 'promedioOportunidades', 'oportunidades'].includes(column))
      .map(key => ({key, label: columnLabel(key)}));
  }
  visibleRows(result: QueryData): Record<string, unknown>[] {
    const filtered = this.filteredRows();
    return filtered?.scope === result.id ? filtered.rows as Record<string, unknown>[] : result.filas;
  }
  readonly graph = signal<GraphData | null>(null);
  readonly sections = GRAPH_SECTIONS;
  readonly queryTitles = QUERY_TITLES;
  readonly columnLabel = columnLabel;
  readonly section = signal(this.sections[0]);
  readonly queryStates = signal<Record<string, QueryState>>({});
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private graphRequest = 0;
  readonly requestedLimit = signal(10);
  limite = 10;
  readonly options = signal<{carreras: string[]; departamentos: string[]}>({carreras: [], departamentos: []});
  readonly optionsError = signal('');
  anio = '';
  carrera = '';
  departamento = '';
  resultado = '';
  constructor() {
    this.loadOptions();
    this.route
      .queryParamMap.pipe(takeUntilDestroyed())
      .subscribe((params) => {
        const section =
          this.sections.find((section) => section.id === params.get('seccion')) ?? this.sections[0];
        this.section.set(section);
        const requested = params.get('consulta');
        const selected = requested && section.queries.includes(requested) ? requested : section.queries[0] ?? null;
        this.selectedQuery.set(selected);
        if (section.id === 'grafo') {
          if (!this.graph() && !this.loading() && !this.error()) this.loadGraph();
        } else {
          if (selected && !this.queryStates()[selected]) this.loadQuery(selected);
        }
      });
  }
  selectQuery(id: string): void {
    if (!this.section().queries.includes(id)) return;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { consulta: id },
      queryParamsHandling: 'merge',
    });
  }
  loadGraph(): void {
    const request = ++this.graphRequest;
    this.requestedLimit.set(this.limite);
    this.loading.set(true);
    this.error.set(null);
    let params = new HttpParams().set('limite', String(this.limite));
    if (this.anio) params = params.set('anio', this.anio);
    if (this.carrera.trim()) params = params.set('carrera', this.carrera.trim());
    if (this.departamento.trim()) params = params.set('departamento', this.departamento.trim());
    if (this.resultado) params = params.set('resultado', this.resultado);
    this.api.get<GraphData>('grafos', params).subscribe({
      next: (data) => {
        if (request !== this.graphRequest) return;
        if (!isGraph(data)) {
          this.error.set('El servidor devolvió un grafo inválido. Ajusta los filtros o reintenta.');
          this.loading.set(false);
          return;
        }
        this.graph.set(data);
        this.loading.set(false);
      },
      error: (e) => {
        if (request !== this.graphRequest) return;
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
    this.limite = 10;
    this.anio = '';
    this.carrera = '';
    this.departamento = '';
    this.resultado = '';
    this.loadGraph();
  }
  value(row: Record<string, unknown>, column: string): string {
    if (column === 'municipio' && typeof row['departamento'] === 'string') {
      return displayMunicipality(String(row[column] ?? ''), row['departamento']);
    }
    return displayCareerName(String(row[column] ?? ''));
  }
  careerLabel(value: string): string {
    const label = displayCareerName(value);
    const same = this.options().carreras.filter(option => displayCareerName(option) === label);
    return same.length > 1 ? `${label} (variante ${same.indexOf(value) + 1})` : label;
  }
  loadOptions(): void {
    this.optionsError.set('');
    this.api.get<{carreras: string[]; departamentos: string[]}>('grafos/opciones').subscribe({
      next: options => this.options.set(options),
      error: () => this.optionsError.set('No se pudieron cargar las opciones.'),
    });
  }
  inputValue(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }
}
