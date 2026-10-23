import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

interface CountByLabel { etiqueta: string; cantidad: number; }
interface RateByLabel { etiqueta: string; aprobadas: number; evaluaciones: number; porcentajeAprobacion: number; }
interface CountByPair { primeraEtiqueta: string; segundaEtiqueta: string; cantidad: number; }
interface ResultsByYear { anioIngreso: number; aprobadas: number; noAprobadas: number; evaluaciones: number; porcentajeAprobacion: number; }
interface DashboardData {
  resumen: { evaluaciones: number; aspirantesUnicos: number; aprobadas: number; porcentajeAprobacion: number; };
  aspirantesPorInstitucion: CountByLabel[]; aprobadasPorMateria: CountByLabel[]; aprobacionPorMateria: RateByLabel[];
  aspirantesPorSexoInstitucion: CountByPair[]; aprobacionPorInstitucion: RateByLabel[]; resultadosPorAnio: ResultsByYear[];
}

@Component({ standalone: true, selector: 'app-count-table', inputs: ['rows'], template: `<table><thead><tr><th>Categoría</th><th>Cantidad</th></tr></thead><tbody>@for (row of rows; track row.etiqueta) {<tr><td>{{ row.etiqueta }}</td><td>{{ row.cantidad }}</td></tr>}</tbody></table>` })
export class CountTable { rows: CountByLabel[] = []; }

@Component({ standalone: true, selector: 'app-rate-table', inputs: ['rows'], template: `<table><thead><tr><th>Categoría</th><th>Aprobadas</th><th>Evaluaciones</th><th>Aprobación</th></tr></thead><tbody>@for (row of rows; track row.etiqueta) {<tr><td>{{ row.etiqueta }}</td><td>{{ row.aprobadas }}</td><td>{{ row.evaluaciones }}</td><td>{{ row.porcentajeAprobacion.toFixed(2) }} %</td></tr>}</tbody></table>` })
export class RateTable { rows: RateByLabel[] = []; }


@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, CountTable, RateTable],
  template: `
    <p class="eyebrow">PANORAMA GENERAL</p>
    <h1>Estadísticas de admisión</h1>
    <p class="page-description">Indicadores calculados sobre las evaluaciones cargadas en MongoDB.</p>
    @if (loading()) { <p class="processing" role="status">Consultando las estadísticas…</p> }
    @if (error()) { <section class="card empty-state"><h2>No se pudieron obtener las estadísticas</h2><p>{{ error() }}</p><button class="button" type="button" (click)="load()">Reintentar</button></section> }
    @if (data(); as dashboard) {
      @if (dashboard.resumen.evaluaciones === 0) {
        <section class="card empty-state"><span class="empty-symbol" aria-hidden="true">↗</span><h2>Tu análisis comienza con los datos</h2>
          <p>Carga archivos CSV de pruebas específicas para consultar las estadísticas de admisión.</p><a class="button" routerLink="/carga">Cargar archivos CSV</a></section>
      } @else {
        <div class="metrics">
          <section class="card metric"><h2>Aspirantes únicos</h2><p class="metric-value">{{ dashboard.resumen.aspirantesUnicos }}</p><span>Correlativos distintos</span></section>
          <section class="card metric"><h2>Evaluaciones</h2><p class="metric-value">{{ dashboard.resumen.evaluaciones }}</p><span>Registros distintos cargados</span></section>
          <section class="card metric"><h2>Aprobación</h2><p class="metric-value">{{ percent(dashboard.resumen.porcentajeAprobacion) }}</p><span>{{ dashboard.resumen.aprobadas }} evaluaciones aprobadas</span></section>
        </div>
        <div class="dashboard-grid">
          <section class="card"><h2>Aspirantes por tipo de institución</h2><p class="hint">M01 · correlativos distintos por tipo.</p><app-count-table [rows]="dashboard.aspirantesPorInstitucion" /></section>
          <section class="card"><h2>Evaluaciones aprobadas por materia</h2><p class="hint">M02 · evaluaciones, no aspirantes.</p><app-count-table [rows]="dashboard.aprobadasPorMateria" /></section>
          <section class="card"><h2>Porcentaje de aprobación por materia</h2><p class="hint">M04 · aprobadas entre evaluaciones de la materia.</p><app-rate-table [rows]="dashboard.aprobacionPorMateria" /></section>
          <section class="card"><h2>Aspirantes por sexo e institución</h2><p class="hint">M12 · correlativos distintos dentro de cada combinación.</p><table><thead><tr><th>Sexo</th><th>Institución</th><th>Cantidad</th></tr></thead><tbody>@for (row of dashboard.aspirantesPorSexoInstitucion; track row.primeraEtiqueta + row.segundaEtiqueta) {<tr><td>{{ row.primeraEtiqueta }}</td><td>{{ row.segundaEtiqueta }}</td><td>{{ row.cantidad }}</td></tr>}</tbody></table></section>
          <section class="card"><h2>Aprobación por tipo de institución</h2><p class="hint">M18 · porcentaje de evaluaciones aprobadas.</p><app-rate-table [rows]="dashboard.aprobacionPorInstitucion" /></section>
          <section class="card"><h2>Resultados por año de ingreso</h2><p class="hint">M20 · distribución de evaluaciones por cohorte.</p><table><thead><tr><th>Año</th><th>Aprobadas</th><th>No aprobadas</th><th>Aprobación</th></tr></thead><tbody>@for (row of dashboard.resultadosPorAnio; track row.anioIngreso) {<tr><td>{{ row.anioIngreso }}</td><td>{{ row.aprobadas }}</td><td>{{ row.noAprobadas }}</td><td>{{ percent(row.porcentajeAprobacion) }}</td></tr>}</tbody></table></section>
        </div>
      }
    }
  `,
})
export class Dashboard {
  private readonly api = inject(ApiService);
  readonly data = signal<DashboardData | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() { this.load(); }
  load(): void {
    this.loading.set(true); this.error.set(null);
    this.api.get<DashboardData>('estadisticas/dashboard').subscribe({
      next: (data) => { this.data.set(data); this.loading.set(false); },
      error: (error: unknown) => { this.error.set(error instanceof ApiError ? error.message : 'No se pudieron cargar los datos.'); this.loading.set(false); },
    });
  }
  percent(value: number): string { return `${value.toFixed(2)} %`; }
}

