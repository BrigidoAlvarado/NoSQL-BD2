import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DASHBOARD_SECTIONS } from './dashboard.sections';
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
  aprobadosPorCarreraAnio: CareerYearCount[]; edadPorCarrera: AverageByLabel[]; resumenCarreras: CareerSummary[];
  edadAprobadosPorCarreraInstitucion: AverageByPair[]; aprobadosPorMunicipioCarrera: CountByTriple[];
  evaluacionesPublicasPorMesMateria: CountByPair[]; topCarrerasJuveniles: CountByLabel[]; aprobacionPorEdad: AgeRate[];
  oportunidadesPorMateria: AverageOpportunities[]; noAprobaronPrimerIntentoPorCarrera: CountByLabel[];
  topMunicipios: CountByPair[]; carrerasMasDemandadasPorDepartamento: CountByPair[];
}
interface CareerYearCount { carrera: string; anioIngreso: number; aspirantes: number; }
interface AverageByLabel { etiqueta: string; promedio: number; conDato: number; sinDato: number; }
interface AverageByPair { primeraEtiqueta: string; segundaEtiqueta: string; promedio: number; conDato: number; sinDato: number; }
interface CountByTriple { primeraEtiqueta: string; segundaEtiqueta: string; terceraEtiqueta: string; cantidad: number; }
interface AgeRate { edad: number; aprobadas: number; evaluaciones: number; porcentajeAprobacion: number; }
interface AverageOpportunities { materia: string; promedio: number; grupos: number; }
interface CareerSummary { anioIngreso: number; carrera: string; evaluaciones: number; aspirantesDistintos: number; aprobadas: number; noAprobadas: number; porcentajeAprobacion: number; sinEdad: number; }
interface ChartDatum { label: string; value: number; }
type ChartKind = 'bar' | 'donut';
interface DashboardQuery {
  id: string; label: string; title: string; hint: string; chart: ChartKind;
  chartData: (data: DashboardData) => ChartDatum[];
}

const queries = (section: string): readonly DashboardQuery[] => QUERY_CATALOG[section] ?? QUERY_CATALOG['panorama'];
const count = (rows: CountByLabel[]) => rows.map(row => ({ label: row.etiqueta, value: row.cantidad }));
const rate = (rows: RateByLabel[]) => rows.map(row => ({ label: row.etiqueta, value: row.porcentajeAprobacion }));
const pair = (rows: CountByPair[]) => rows.map(row => ({ label: `${row.primeraEtiqueta} · ${row.segundaEtiqueta}`, value: row.cantidad }));

const QUERY_CATALOG: Record<string, readonly DashboardQuery[]> = {
  panorama: [
    { id: 'institucion', label: 'Por tipo de institución', title: 'Aspirantes por tipo de institución', hint: 'M01 · correlativos distintos por tipo.', chart: 'donut', chartData: d => count(d.aspirantesPorInstitucion) },
    { id: 'sexo-institucion', label: 'Por sexo e institución', title: 'Aspirantes por sexo e institución', hint: 'M12 · correlativos distintos dentro de cada combinación.', chart: 'bar', chartData: d => pair(d.aspirantesPorSexoInstitucion) },
    { id: 'resultados-anio', label: 'Resultados por año', title: 'Resultados por año de ingreso', hint: 'M20 · distribución de evaluaciones por cohorte.', chart: 'donut', chartData: d => [{ label: 'Aprobadas', value: d.resultadosPorAnio.reduce((sum, row) => sum + row.aprobadas, 0) }, { label: 'No aprobadas', value: d.resultadosPorAnio.reduce((sum, row) => sum + row.noAprobadas, 0) }] },
  ],
  aprobacion: [
    { id: 'aprobadas-materia', label: 'Aprobadas por materia', title: 'Evaluaciones aprobadas por materia', hint: 'M02 · evaluaciones, no aspirantes.', chart: 'bar', chartData: d => count(d.aprobadasPorMateria) },
    { id: 'porcentaje-materia', label: 'Porcentaje por materia', title: 'Porcentaje de aprobación por materia', hint: 'M04 · aprobadas entre evaluaciones de la materia.', chart: 'bar', chartData: d => rate(d.aprobacionPorMateria) },
    { id: 'porcentaje-institucion', label: 'Por institución', title: 'Aprobación por tipo de institución', hint: 'M18 · porcentaje de evaluaciones aprobadas.', chart: 'bar', chartData: d => rate(d.aprobacionPorInstitucion) },
    { id: 'oportunidades', label: 'Oportunidades por materia', title: 'Oportunidades usadas por materia', hint: 'M14 · oportunidades distintas por aspirante, cohorte y materia.', chart: 'bar', chartData: d => d.oportunidadesPorMateria.map(row => ({ label: row.materia, value: row.promedio })) },
  ],
  carreras: [
    { id: 'aprobados-carrera', label: 'Aprobados por carrera y año', title: 'Aspirantes aprobados por carrera y año', hint: 'M03 · correlativos con alguna aprobación.', chart: 'bar', chartData: d => d.aprobadosPorCarreraAnio.map(row => ({ label: `${row.carrera} · ${row.anioIngreso}`, value: row.aspirantes })) },
    { id: 'resumen-carrera', label: 'Resumen por carrera', title: 'Resumen por carrera', hint: 'M06 · colección materializada resumen_carrera.', chart: 'bar', chartData: d => d.resumenCarreras.map(row => ({ label: `${row.carrera} · ${row.anioIngreso}`, value: row.aspirantesDistintos })) },
    { id: 'juveniles', label: 'Top carreras 16–18', title: 'Top carreras de aspirantes de 16 a 18', hint: 'M10 · top 5, excluye carrera desconocida.', chart: 'bar', chartData: d => count(d.topCarrerasJuveniles) },
    { id: 'primer-intento', label: 'Primer intento no aprobado', title: 'No aprobaron primer intento', hint: 'M16 · oportunidad 1 por carrera.', chart: 'bar', chartData: d => count(d.noAprobaronPrimerIntentoPorCarrera) },
  ],
  perfil: [
    { id: 'edad-carrera', label: 'Edad por carrera', title: 'Edad promedio por carrera', hint: 'M05 · una edad por aspirante y carrera.', chart: 'bar', chartData: d => d.edadPorCarrera.map(row => ({ label: row.etiqueta, value: row.promedio })) },
    { id: 'edad-aprobados', label: 'Edad de aprobados', title: 'Edad de aprobados por carrera e institución', hint: 'M07 · promedio sin ponderar intentos.', chart: 'bar', chartData: d => d.edadAprobadosPorCarreraInstitucion.map(row => ({ label: `${row.primeraEtiqueta} · ${row.segundaEtiqueta}`, value: row.promedio })) },
    { id: 'aprobacion-edad', label: 'Aprobación por edad', title: 'Aprobación por edad', hint: 'M13 · solo edades válidas.', chart: 'bar', chartData: d => d.aprobacionPorEdad.map(row => ({ label: `${row.edad} años`, value: row.porcentajeAprobacion })) },
  ],
  procedencia: [
    { id: 'municipio-carrera', label: 'Aprobados por municipio', title: 'Aprobados por municipio y carrera', hint: 'M08 · municipio conserva su departamento.', chart: 'bar', chartData: d => d.aprobadosPorMunicipioCarrera.map(row => ({ label: `${row.segundaEtiqueta} · ${row.terceraEtiqueta}`, value: row.cantidad })) },
    { id: 'publicas-mes', label: 'Públicas por mes y materia', title: 'Evaluaciones públicas por mes y materia', hint: 'M09 · fecha de asignación.', chart: 'bar', chartData: d => pair(d.evaluacionesPublicasPorMesMateria) },
    { id: 'municipios', label: 'Top municipios', title: 'Top municipios de procedencia', hint: 'M17 · top 5, conserva departamento.', chart: 'bar', chartData: d => pair(d.topMunicipios) },
    { id: 'demanda-departamento', label: 'Demanda por departamento', title: 'Carreras más demandadas por departamento', hint: 'M19 · incluye empates.', chart: 'bar', chartData: d => pair(d.carrerasMasDemandadasPorDepartamento) },
  ],
};

@Component({ standalone: true, selector: 'app-metric-chart', inputs: ['rows', 'kind', 'title'], template: `
  @if (!rows.length) { <p class="empty-result">No hay datos para graficar.</p> } @else {
    <figure class="metric-chart" [class.donut-chart]="kind === 'donut'" [attr.aria-label]="title">
      @if (kind === 'donut') { <div class="donut" [style.background]="donutBackground()"><span>{{format(total())}}</span><small>registros</small></div> }
      <figcaption><span class="chart-caption">{{ kind === 'donut' ? 'Distribución del total' : 'Comparación de valores' }}</span>
        <ul class="chart-legend">@for (row of visibleRows(); track row.label; let index = $index) {<li><span [style.background]="color(index)"></span><span [title]="row.label">{{row.label}}</span><strong>{{format(row.value)}}</strong></li>}</ul>
      </figcaption>
      @if (kind === 'bar') { <div class="chart-bars">@for (row of visibleRows(); track row.label; let index = $index) {<div><span [title]="row.label">{{row.label}}</span><span class="chart-bar-track"><span [style.width.%]="width(row.value)" [style.background]="color(index)"></span></span><strong>{{format(row.value)}}</strong></div>}</div> }
    </figure>
  }` })
export class MetricChart {
  rows: ChartDatum[] = []; kind: ChartKind = 'bar'; title = '';
  readonly palette = ['#2d72b7', '#28745e', '#a7642d', '#7652a2', '#ad4662', '#3c8095', '#7a6c37', '#4c6381'];
  visibleRows(): ChartDatum[] { return this.rows.slice(0, 8); }
  total(): number { return this.visibleRows().reduce((sum, row) => sum + row.value, 0); }
  width(value: number): number { const max = Math.max(0, ...this.visibleRows().map(row => row.value)); return max ? value / max * 100 : 0; }
  color(index: number): string { return this.palette[index % this.palette.length]; }
  format(value: number): string { return Number.isInteger(value) ? String(value) : value.toFixed(2); }
  donutBackground(): string { const rows = this.visibleRows(); const total = this.total(); if (!total) return '#e7edf4'; let current = 0; return `conic-gradient(${rows.map((row, index) => { const start = current; current += row.value / total * 100; return `${this.color(index)} ${start}% ${current}%`; }).join(', ')})`; }
}

@Component({ standalone: true, selector: 'app-count-table', inputs: ['rows'], template: `@if (!rows.length) {<p class="empty-result">No hay datos para esta consulta.</p>} @else {<table><thead><tr><th>Categoría</th><th>Cantidad</th></tr></thead><tbody>@for (row of rows; track row.etiqueta) {<tr><td><span>{{ row.etiqueta }}</span><span class="comparison-track" aria-hidden="true"><span [style.width.%]="width(row.cantidad)"></span></span></td><td class="numeric">{{ row.cantidad }}</td></tr>}</tbody></table>}` })
export class CountTable {
  rows: CountByLabel[] = [];
  width(value: number): number { const max = Math.max(0, ...this.rows.map(row => row.cantidad)); return max > 0 ? value / max * 100 : 0; }
}

@Component({ standalone: true, selector: 'app-rate-table', inputs: ['rows'], template: `@if (!rows.length) {<p class="empty-result">No hay datos para esta consulta.</p>} @else {<table><thead><tr><th>Categoría</th><th>Aprobadas</th><th>Evaluaciones</th><th>Aprobación</th></tr></thead><tbody>@for (row of rows; track row.etiqueta) {<tr><td>{{ row.etiqueta }}</td><td>{{ row.aprobadas }}</td><td>{{ row.evaluaciones }}</td><td class="numeric">{{ row.porcentajeAprobacion.toFixed(2) }} %<progress class="approval-progress" [value]="row.porcentajeAprobacion" max="100" [attr.aria-label]="'Aprobación de ' + row.etiqueta"></progress></td></tr>}</tbody></table>}` })
export class RateTable { rows: RateByLabel[] = []; }

@Component({ standalone: true, selector: 'app-pair-table', inputs: ['rows', 'firstLabel', 'secondLabel'], template: `@if (!rows.length) {<p class="empty-result">No hay datos para esta consulta.</p>} @else {<table><thead><tr><th>{{firstLabel}}</th><th>{{secondLabel}}</th><th>Cantidad</th></tr></thead><tbody>@for (r of rows; track r.primeraEtiqueta + r.segundaEtiqueta) {<tr><td>{{r.primeraEtiqueta}}</td><td>{{r.segundaEtiqueta}}</td><td>{{r.cantidad}}</td></tr>}</tbody></table>}` })
export class PairTable { rows: CountByPair[] = []; firstLabel = 'Departamento'; secondLabel = 'Municipio'; }
@Component({ standalone: true, selector: 'app-triple-table', inputs: ['rows'], template: `<table><thead><tr><th>Departamento</th><th>Municipio</th><th>Carrera</th><th>Aspirantes</th></tr></thead><tbody>@for (r of rows; track r.primeraEtiqueta + r.segundaEtiqueta + r.terceraEtiqueta) {<tr><td>{{r.primeraEtiqueta}}</td><td>{{r.segundaEtiqueta}}</td><td>{{r.terceraEtiqueta}}</td><td>{{r.cantidad}}</td></tr>}</tbody></table>` })
export class TripleTable { rows: CountByTriple[] = []; }


@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, CountTable, RateTable, PairTable, TripleTable, MetricChart],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly params = toSignal(this.route.queryParamMap);
  readonly sections = DASHBOARD_SECTIONS;
  readonly section = computed(() => this.sections.find(section => section.id === this.params()?.get('seccion')) ?? this.sections[0]);
  readonly sectionQueries = computed(() => queries(this.section().id));
  readonly selectedQuery = computed(() => this.sectionQueries().find(query => query.id === this.params()?.get('consulta')) ?? this.sectionQueries()[0]);
  readonly data = signal<DashboardData | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() { this.load(); }
  load(): void {
    this.loading.set(true); this.error.set(null);
    this.api.get<DashboardData>('estadisticas/dashboard').subscribe({
      next: (data) => {
        if (!isDashboard(data)) { this.error.set('El servidor devolvió estadísticas incompletas. Puedes reintentar la consulta.'); this.loading.set(false); return; }
        this.data.set(data); this.loading.set(false);
      },
      error: (error: unknown) => { this.error.set(error instanceof ApiError ? error.message : 'No se pudieron cargar los datos.'); this.loading.set(false); },
    });
  }
  percent(value: number): string { return `${value.toFixed(2)} %`; }
  selectQuery(queryId: string): void { this.router.navigate([], { relativeTo: this.route, queryParams: { consulta: queryId }, queryParamsHandling: 'merge' }); }
  chartRows(data: DashboardData): ChartDatum[] { return this.selectedQuery().chartData(data); }
}

function isDashboard(value: unknown): value is DashboardData {
  if (typeof value !== 'object' || value === null) return false;
  const data = value as Record<string, unknown>;
  const arrays = ['aspirantesPorInstitucion', 'aprobadasPorMateria', 'aprobacionPorMateria', 'aspirantesPorSexoInstitucion', 'aprobacionPorInstitucion', 'resultadosPorAnio', 'aprobadosPorCarreraAnio', 'edadPorCarrera', 'resumenCarreras', 'edadAprobadosPorCarreraInstitucion', 'aprobadosPorMunicipioCarrera', 'evaluacionesPublicasPorMesMateria', 'topCarrerasJuveniles', 'aprobacionPorEdad', 'oportunidadesPorMateria', 'noAprobaronPrimerIntentoPorCarrera', 'topMunicipios', 'carrerasMasDemandadasPorDepartamento'];
  return typeof data['resumen'] === 'object' && data['resumen'] !== null && arrays.every(key => Array.isArray(data[key]));
}

