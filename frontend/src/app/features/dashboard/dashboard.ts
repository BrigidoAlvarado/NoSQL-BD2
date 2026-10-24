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

@Component({ standalone: true, selector: 'app-count-table', inputs: ['rows'], template: `<table><thead><tr><th>Categoría</th><th>Cantidad</th></tr></thead><tbody>@for (row of rows; track row.etiqueta) {<tr><td>{{ row.etiqueta }}</td><td>{{ row.cantidad }}</td></tr>}</tbody></table>` })
export class CountTable { rows: CountByLabel[] = []; }

@Component({ standalone: true, selector: 'app-rate-table', inputs: ['rows'], template: `<table><thead><tr><th>Categoría</th><th>Aprobadas</th><th>Evaluaciones</th><th>Aprobación</th></tr></thead><tbody>@for (row of rows; track row.etiqueta) {<tr><td>{{ row.etiqueta }}</td><td>{{ row.aprobadas }}</td><td>{{ row.evaluaciones }}</td><td>{{ row.porcentajeAprobacion.toFixed(2) }} %</td></tr>}</tbody></table>` })
export class RateTable { rows: RateByLabel[] = []; }

@Component({ standalone: true, selector: 'app-pair-table', inputs: ['rows'], template: `<table><thead><tr><th>Grupo 1</th><th>Grupo 2</th><th>Cantidad</th></tr></thead><tbody>@for (r of rows; track r.primeraEtiqueta + r.segundaEtiqueta) {<tr><td>{{r.primeraEtiqueta}}</td><td>{{r.segundaEtiqueta}}</td><td>{{r.cantidad}}</td></tr>}</tbody></table>` })
export class PairTable { rows: CountByPair[] = []; }
@Component({ standalone: true, selector: 'app-triple-table', inputs: ['rows'], template: `<table><thead><tr><th>Departamento</th><th>Municipio</th><th>Carrera</th><th>Aspirantes</th></tr></thead><tbody>@for (r of rows; track r.primeraEtiqueta + r.segundaEtiqueta + r.terceraEtiqueta) {<tr><td>{{r.primeraEtiqueta}}</td><td>{{r.segundaEtiqueta}}</td><td>{{r.terceraEtiqueta}}</td><td>{{r.cantidad}}</td></tr>}</tbody></table>` })
export class TripleTable { rows: CountByTriple[] = []; }


@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, CountTable, RateTable, PairTable, TripleTable],
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
          <section class="card"><h2>Aspirantes aprobados por carrera y año</h2><p class="hint">M03 · correlativos con alguna aprobación.</p><table><thead><tr><th>Carrera</th><th>Año</th><th>Aspirantes</th></tr></thead><tbody>@for(r of dashboard.aprobadosPorCarreraAnio;track r.carrera+r.anioIngreso){<tr><td>{{r.carrera}}</td><td>{{r.anioIngreso}}</td><td>{{r.aspirantes}}</td></tr>}</tbody></table></section>
          <section class="card"><h2>Edad promedio por carrera</h2><p class="hint">M05 · una edad por aspirante y carrera.</p><table><thead><tr><th>Carrera</th><th>Promedio</th><th>Con edad</th><th>Sin edad</th></tr></thead><tbody>@for(r of dashboard.edadPorCarrera;track r.etiqueta){<tr><td>{{r.etiqueta}}</td><td>{{r.promedio.toFixed(2)}}</td><td>{{r.conDato}}</td><td>{{r.sinDato}}</td></tr>}</tbody></table></section>
          <section class="card"><h2>Resumen por carrera</h2><p class="hint">M06 · colección materializada resumen_carrera.</p><table><thead><tr><th>Año</th><th>Carrera</th><th>Evaluaciones</th><th>Aspirantes</th><th>Aprobación</th></tr></thead><tbody>@for(r of dashboard.resumenCarreras;track r.anioIngreso+r.carrera){<tr><td>{{r.anioIngreso}}</td><td>{{r.carrera}}</td><td>{{r.evaluaciones}}</td><td>{{r.aspirantesDistintos}}</td><td>{{percent(r.porcentajeAprobacion)}}</td></tr>}</tbody></table></section>
          <section class="card"><h2>Edad de aprobados por carrera e institución</h2><p class="hint">M07 · promedio sin ponderar intentos.</p><table><thead><tr><th>Carrera</th><th>Institución</th><th>Promedio</th><th>Sin edad</th></tr></thead><tbody>@for(r of dashboard.edadAprobadosPorCarreraInstitucion;track r.primeraEtiqueta+r.segundaEtiqueta){<tr><td>{{r.primeraEtiqueta}}</td><td>{{r.segundaEtiqueta}}</td><td>{{r.promedio.toFixed(2)}}</td><td>{{r.sinDato}}</td></tr>}</tbody></table></section>
          <section class="card"><h2>Aprobados por municipio y carrera</h2><p class="hint">M08 · municipio conserva su departamento.</p><app-triple-table [rows]="dashboard.aprobadosPorMunicipioCarrera" /></section>
          <section class="card"><h2>Evaluaciones públicas por mes y materia</h2><p class="hint">M09 · fecha de asignación.</p><app-pair-table [rows]="dashboard.evaluacionesPublicasPorMesMateria" /></section>
          <section class="card"><h2>Top carreras de aspirantes de 16 a 18</h2><p class="hint">M10 · top 5, excluye carrera desconocida.</p><app-count-table [rows]="dashboard.topCarrerasJuveniles" /></section>
          <section class="card"><h2>Aprobación por edad</h2><p class="hint">M13 · solo edades válidas.</p><table><thead><tr><th>Edad</th><th>Aprobadas</th><th>Evaluaciones</th><th>Aprobación</th></tr></thead><tbody>@for(r of dashboard.aprobacionPorEdad;track r.edad){<tr><td>{{r.edad}}</td><td>{{r.aprobadas}}</td><td>{{r.evaluaciones}}</td><td>{{percent(r.porcentajeAprobacion)}}</td></tr>}</tbody></table></section>
          <section class="card"><h2>Oportunidades usadas por materia</h2><p class="hint">M14 · oportunidades distintas por aspirante, cohorte y materia.</p><table><thead><tr><th>Materia</th><th>Promedio</th><th>Grupos</th></tr></thead><tbody>@for(r of dashboard.oportunidadesPorMateria;track r.materia){<tr><td>{{r.materia}}</td><td>{{r.promedio.toFixed(2)}}</td><td>{{r.grupos}}</td></tr>}</tbody></table></section>
          <section class="card"><h2>No aprobaron primer intento</h2><p class="hint">M16 · oportunidad 1 por carrera.</p><app-count-table [rows]="dashboard.noAprobaronPrimerIntentoPorCarrera" /></section>
          <section class="card"><h2>Top municipios de procedencia</h2><p class="hint">M17 · top 5, conserva departamento.</p><app-pair-table [rows]="dashboard.topMunicipios" /></section>
          <section class="card"><h2>Carreras más demandadas por departamento</h2><p class="hint">M19 · incluye empates.</p><app-pair-table [rows]="dashboard.carrerasMasDemandadasPorDepartamento" /></section>
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

