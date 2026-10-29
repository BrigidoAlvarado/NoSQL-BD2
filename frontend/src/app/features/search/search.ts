import { HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

interface EvaluationResult { aspiranteId: string; fechaAsignacion: string; materia: string; oportunidad: number; anioIngreso: number; aprobacion: string; carreraObjetivo: string; tipoInstitucion: string; municipio: string; departamento: string; }
interface SearchData { criterio: string; termino: string; totalDevuelto: number; resultados: EvaluationResult[]; }
interface ApplicantEvaluation extends EvaluationResult { sexo: string; anioNacimiento: number | null; edadIngreso: number | null; }
interface ApplicantDetail { aspiranteId: string; totalEvaluaciones: number; evaluacionesAprobadas: number; evaluaciones: ApplicantEvaluation[]; }

@Component({
  selector: 'app-search',
  template: `
    <p class="eyebrow">CONSULTA DE INFORMACIÓN</p><h1>Búsqueda de aspirantes</h1>
    <p class="page-description">Encuentra evaluaciones y consulta el historial completo de cada correlativo anónimo.</p>
    <section class="card search-form"><h2>Buscar evaluaciones</h2>
      <p class="hint">La fuente no identifica centros educativos por nombre; “institución” busca por tipo de institución educativa.</p>
      <div class="search-controls"><label>Criterio<select [value]="criterio" (change)="criterio=inputValue($event)">
        <option value="aspirante">Correlativo del aspirante</option><option value="carrera">Carrera objetivo</option><option value="institucion">Tipo de institución educativa</option><option value="materia">Materia</option><option value="municipio">Municipio</option><option value="departamento">Departamento</option>
      </select></label><label class="search-term">Término<input [value]="termino" (input)="termino=inputValue($event)" (keyup.enter)="search()" [placeholder]="placeholder()" aria-describedby="search-help"></label><button class="button" type="button" (click)="search()" [disabled]="loading()">{{loading() ? 'Consultando…' : 'Buscar'}}</button></div>
      <p id="search-help" class="hint">{{criterio === 'aspirante' ? 'Escribe el correlativo completo para encontrar a un aspirante.' : 'Puedes escribir parte del texto. No se distinguen mayúsculas de minúsculas.'}}</p>
    </section>
    @if (!results() && !loading() && !error()) { <section class="card empty-state"><span class="empty-symbol" aria-hidden="true">⌕</span><h2>Encuentra una evaluación</h2><p>Elige un criterio y escribe un término. En cada coincidencia podrás abrir el historial completo del aspirante.</p></section> }
    @if (error()) { <p class="error-message" role="alert">{{error()}}</p> }
    @if (loading()) { <p class="processing" role="status">Consultando evaluaciones…</p> }
    @if (results(); as data) { <section class="card search-results"><div class="result-heading"><h2>Coincidencias para «{{data.termino}}»</h2><span class="tag">{{data.totalDevuelto}} evaluaciones mostradas</span></div>
      @if(data.totalDevuelto >= 50) {<p class="hint">Se muestran hasta 50 evaluaciones. Usa un término más específico para acotar la búsqueda.</p>}
      @if (!data.resultados.length) { <p class="empty-result">No se encontraron evaluaciones con ese criterio.</p> } @else { <div class="table-scroll"><table><thead><tr><th>Aspirante</th><th>Fecha</th><th>Materia</th><th>Intento</th><th>Resultado</th><th>Carrera</th><th></th></tr></thead><tbody>@for (row of data.resultados; track row.aspiranteId + row.fechaAsignacion + row.materia + row.oportunidad) { <tr><td>{{row.aspiranteId}}</td><td>{{row.fechaAsignacion}}</td><td>{{row.materia}}</td><td>{{row.oportunidad}}</td><td>{{row.aprobacion}}</td><td>{{row.carreraObjetivo}}</td><td><button class="text-button" type="button" (click)="loadApplicant(row.aspiranteId)">Ver historial</button></td></tr> }</tbody></table></div> }
    </section> }
    @if (applicant(); as detail) { <section class="card applicant-detail"><div class="result-heading"><div><h2>Historial del aspirante</h2><p class="hint">Correlativo: {{detail.aspiranteId}}</p></div><span class="tag">{{detail.evaluacionesAprobadas}} aprobadas de {{detail.totalEvaluaciones}}</span></div>
      <p class="hint">Cada fila es una evaluación distinta; una persona puede tener varias materias, oportunidades y años de ingreso.</p><div class="table-scroll"><table><thead><tr><th>Fecha</th><th>Sexo</th><th>Año nac.</th><th>Edad</th><th>Materia</th><th>Intento</th><th>Año ingreso</th><th>Resultado</th><th>Carrera</th><th>Tipo institución</th><th>Municipio</th><th>Departamento</th></tr></thead><tbody>@for (row of detail.evaluaciones; track row.fechaAsignacion + row.materia + row.oportunidad + row.anioIngreso) { <tr><td>{{row.fechaAsignacion}}</td><td>{{row.sexo}}</td><td>{{display(row.anioNacimiento)}}</td><td>{{display(row.edadIngreso)}}</td><td>{{row.materia}}</td><td>{{row.oportunidad}}</td><td>{{row.anioIngreso}}</td><td>{{row.aprobacion}}</td><td>{{row.carreraObjetivo}}</td><td>{{row.tipoInstitucion}}</td><td>{{row.municipio}}</td><td>{{row.departamento}}</td></tr> }</tbody></table></div>
    </section> }
  `,
})
export class Search {
  private readonly api = inject(ApiService);
  readonly results = signal<SearchData | null>(null); readonly applicant = signal<ApplicantDetail | null>(null);
  readonly loading = signal(false); readonly error = signal<string | null>(null);
  criterio = 'aspirante'; termino = '';

  search(): void {
    const term = this.termino.trim();
    this.applicant.set(null); this.error.set(null);
    if (!term) { this.results.set(null); this.error.set('Ingresa un término para realizar la búsqueda.'); return; }
    this.loading.set(true);
    const params = new HttpParams().set('criterio', this.criterio).set('termino', term).set('limite', '50');
    this.api.get<SearchData>('busqueda', params).subscribe({
      next: data => { if (!isSearch(data)) { this.error.set('El servidor devolvió resultados de búsqueda inválidos. Puedes reintentar.'); this.loading.set(false); return; } this.results.set(data); this.loading.set(false); },
      error: error => { this.error.set(error instanceof ApiError ? error.message : 'No se pudo completar la búsqueda.'); this.loading.set(false); },
    });
  }
  loadApplicant(id: string): void {
    this.error.set(null); this.loading.set(true);
    this.api.get<ApplicantDetail>(`busqueda/aspirantes/${encodeURIComponent(id)}`).subscribe({
      next: data => { if (!isApplicant(data)) { this.error.set('El servidor devolvió un historial inválido. Puedes reintentar.'); this.loading.set(false); return; } this.applicant.set(data); this.loading.set(false); },
      error: error => { this.error.set(error instanceof ApiError ? error.message : 'No se pudo consultar el historial.'); this.loading.set(false); },
    });
  }
  inputValue(event: Event): string { return (event.target as HTMLInputElement).value; }
  placeholder(): string { return ({aspirante: 'Pega el correlativo anónimo completo', carrera: 'Ej. SISTEMAS', institucion: 'Ej. PRIVADO', materia: 'Ej. MATEMATICA', municipio: 'Ej. MIXCO', departamento: 'Ej. GUATEMALA'} as Record<string, string>)[this.criterio] ?? ''; }
  display(value: number | null): string { return value === null ? 'SIN REGISTRO' : String(value); }
}

function isSearch(value: unknown): value is SearchData { return typeof value === 'object' && value !== null && Array.isArray((value as SearchData).resultados) && typeof (value as SearchData).totalDevuelto === 'number'; }
function isApplicant(value: unknown): value is ApplicantDetail { return typeof value === 'object' && value !== null && typeof (value as ApplicantDetail).aspiranteId === 'string' && Array.isArray((value as ApplicantDetail).evaluaciones); }
