import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

interface RejectedRow { line: number; code: string; detail: string; }
interface UploadFileResult {
  nombreArchivo: string; sha256Archivo: string | null; estado: string;
  registrosLeidos: number; registrosProcesados: number; registrosRechazados: number;
  duracionMilisegundos: number; reutilizada: boolean; detalle: string; rechazos: RejectedRow[];
}
interface UploadResponse { archivos: UploadFileResult[]; mensaje: string | null; }

@Component({
  selector: 'app-upload',
  template: `
    <p class="eyebrow">IMPORTACIÓN DE INFORMACIÓN</p>
    <h1>Carga de datos</h1>
    <p class="page-description">Carga uno o varios CSV para validarlos y sincronizarlos con MongoDB y Neo4j.</p>
    <section class="card">
      <h2>Selecciona tus archivos</h2>
      <p>Selecciona archivos con extensión .csv y contenido no vacío.</p>
      <label class="file-label" for="csv-files">Archivos CSV</label>
      <input id="csv-files" type="file" accept=".csv,text/csv" multiple aria-describedby="upload-help"
        [disabled]="loading()" (change)="selectFiles($event)">
      <p id="upload-help" class="hint">Los archivos se envían al backend, que aplica las validaciones y registra el resultado por archivo.</p>
      @if (errors().length) {
        <div class="error-message" role="alert"><ul>
          @for (error of errors(); track $index) { <li>{{ error }}</li> }
        </ul></div>
      }
      @if (files().length) {
        <div aria-live="polite"><h3>Archivos seleccionados: {{ files().length }}</h3>
          <ul class="file-list">
            @for (file of files(); track file.name) { <li><span>{{ file.name }}</span><span>{{ sizeLabel(file.size) }}</span></li> }
          </ul>
        </div>
        <div class="actions">
          <button class="button" type="button" [disabled]="loading()" (click)="upload()">{{ loading() ? 'Procesando…' : 'Cargar y procesar' }}</button>
          <button class="button secondary" type="button" [disabled]="loading()" (click)="clearFiles()">Limpiar selección</button>
        </div>
      }
    </section>
    @if (loading()) { <p class="processing" role="status">El backend está validando y sincronizando los archivos. Esto puede tardar unos minutos.</p> }
    @if (results().length) {
      <section class="card results-card" aria-live="polite"><h2>Resultado de la carga</h2>
        @for (result of results(); track result.nombreArchivo) {
          <article class="upload-result">
            <div class="result-heading"><h3>{{ result.nombreArchivo }}</h3><span class="state" [class.error-state]="isError(result.estado)">{{ stateLabel(result.estado) }}</span></div>
            <dl class="upload-metrics">
              <div><dt>Leídos</dt><dd>{{ result.registrosLeidos }}</dd></div><div><dt>Procesados</dt><dd>{{ result.registrosProcesados }}</dd></div>
              <div><dt>Rechazados</dt><dd>{{ result.registrosRechazados }}</dd></div><div><dt>Duración</dt><dd>{{ durationLabel(result.duracionMilisegundos) }}</dd></div>
            </dl>
            <p class="hint">{{ result.detalle }} @if (result.reutilizada) { Este archivo ya estaba sincronizado. }</p>
            @if (result.rechazos.length) {
              <details><summary>Ver rechazos y causas ({{ result.rechazos.length }})</summary><ul class="rejection-list">
                @for (rejection of result.rechazos; track rejection.line + rejection.code) { <li><strong>Línea {{ rejection.line }} · {{ rejection.code }}</strong><br>{{ rejection.detail }}</li> }
              </ul></details>
            }
          </article>
        }
      </section>
    }
  `,
})
export class Upload {
  private readonly api = inject(ApiService);
  readonly files = signal<File[]>([]);
  readonly errors = signal<string[]>([]);
  readonly results = signal<UploadFileResult[]>([]);
  readonly loading = signal(false);
  private fileInput?: HTMLInputElement;

  selectFiles(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fileInput = input;
    const valid: File[] = [];
    const errors: string[] = [];
    for (const file of Array.from(input.files ?? [])) {
      if (!file.name.toLowerCase().endsWith('.csv')) errors.push(`${file.name}: el archivo debe tener extensión .csv.`);
      else if (file.size === 0) errors.push(`${file.name}: el archivo está vacío.`);
      else valid.push(file);
    }
    this.files.set(valid);
    this.errors.set(errors);
    this.results.set([]);
  }

  upload(): void {
    if (!this.files().length || this.loading()) return;
    const data = new FormData();
    this.files().forEach((file) => data.append('files', file, file.name));
    this.loading.set(true);
    this.errors.set([]);
    this.api.post<UploadResponse>('cargas', data).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (response) => { this.results.set(response.archivos); if (response.mensaje) this.errors.set([response.mensaje]); },
      error: (error: unknown) => this.errors.set([error instanceof ApiError ? error.message : 'No se pudo enviar la carga.']),
    });
  }

  clearFiles(input = this.fileInput): void {
    if (input) input.value = '';
    this.files.set([]); this.errors.set([]); this.results.set([]);
  }

  sizeLabel(bytes: number): string { return bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`; }
  durationLabel(milliseconds: number): string { return milliseconds < 1000 ? `${milliseconds} ms` : `${(milliseconds / 1000).toFixed(1)} s`; }
  isError(state: string): boolean { return ['RECHAZADA', 'FALLO_MONGO', 'PENDIENTE_NEO4J', 'ARCHIVO_VACIO', 'ARCHIVO_INVALIDO', 'ERROR_ARCHIVO'].includes(state); }

  stateLabel(state: string): string {
    const labels: Record<string, string> = { COMPLETA: 'Completada', YA_COMPLETA: 'Ya procesada', RECHAZADA: 'Rechazada', SIN_FILAS_VALIDAS: 'Sin filas válidas', FALLO_MONGO: 'Falló MongoDB', PENDIENTE_NEO4J: 'Pendiente Neo4j', ARCHIVO_VACIO: 'Archivo vacío', ARCHIVO_INVALIDO: 'Archivo inválido', ERROR_ARCHIVO: 'Error de lectura' };
    return labels[state] ?? state;
  }
}
