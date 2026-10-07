import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-upload',
  template: `
    <p class="eyebrow">IMPORTACIÓN DE INFORMACIÓN</p>
    <h1>Carga de datos</h1>
    <p class="page-description">Prepara uno o varios archivos CSV de evaluaciones de aspirantes.</p>
    <section class="card">
      <h2>Selecciona tus archivos</h2>
      <p>Selecciona archivos con extensión .csv y contenido no vacío.</p>
      <label class="file-label" for="csv-files">Archivos CSV</label>
      <input id="csv-files" type="file" accept=".csv,text/csv" multiple
        aria-describedby="upload-help" (change)="selectFiles($event)">
      <p id="upload-help" class="hint">La selección es local. El envío y procesamiento estarán disponibles al integrar el servicio de carga.</p>
      @if (errors().length) {
        <div class="error-message" role="alert"><ul>
          @for (error of errors(); track $index) { <li>{{ error }}</li> }
        </ul></div>
      }
      @if (files().length) {
        <div aria-live="polite"><h3>Archivos seleccionados: {{ files().length }}</h3>
          <ul class="file-list">
            @for (file of files(); track $index) {
              <li><span>{{ file.name }}</span><span>{{ sizeLabel(file.size) }}</span></li>
            }
          </ul>
        </div>
        <button class="button secondary" type="button" (click)="clearFiles()">Limpiar selección</button>
      }
    </section>
    <section class="card info-card"><h2>Resultado de la carga</h2>
      <p>Cada archivo mostrará registros leídos, procesados y rechazados, tiempo de procesamiento y estado final cuando se habilite la carga.</p>
    </section>
  `,
})
export class Upload {
  readonly files = signal<File[]>([]);
  readonly errors = signal<string[]>([]);
  private fileInput?: HTMLInputElement;

  selectFiles(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fileInput = input;
    const valid: File[] = [];
    const errors: string[] = [];
    for (const file of Array.from(input.files ?? [])) {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        errors.push(`${file.name}: el archivo debe tener extensión .csv.`);
      } else if (file.size === 0) {
        errors.push(`${file.name}: el archivo está vacío.`);
      } else {
        valid.push(file);
      }
    }
    this.files.set(valid);
    this.errors.set(errors);
  }

  clearFiles(input = this.fileInput): void {
    if (input) input.value = '';
    this.files.set([]);
    this.errors.set([]);
  }

  sizeLabel(bytes: number): string {
    return bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`;
  }
}
