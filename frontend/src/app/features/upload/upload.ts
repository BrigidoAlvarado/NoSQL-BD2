import { LoadHistory } from './load-history';
import { Component, effect, inject, untracked, viewChild } from '@angular/core';
import { UploadSession } from './upload-session';
import { UploadProgressView } from './upload-progress';

@Component({
  selector: 'app-upload',
  imports: [LoadHistory, UploadProgressView],
  templateUrl: './upload.html',
})
export class Upload {
  readonly session = inject(UploadSession);
  readonly history = viewChild(LoadHistory);
  readonly files = this.session.files;
  readonly errors = this.session.errors;
  readonly results = this.session.results;
  readonly loading = this.session.loading;
  private fileInput?: HTMLInputElement;

  constructor() {
    let previousResults = this.results();
    effect(() => {
      const results = this.results();
      if (results !== previousResults && results.length) {
        untracked(() => this.history()?.open('cargas'));
      }
      previousResults = results;
    });
  }

  selectFiles(event: Event): void {
    if (this.session.blocked()) return;
    const input = event.target as HTMLInputElement;
    this.fileInput = input;
    const valid: File[] = [];
    const errors: string[] = [];
    for (const file of Array.from(input.files ?? [])) {
      if (!file.name.toLowerCase().endsWith('.csv'))
        errors.push(`${file.name}: el archivo debe tener extensión .csv.`);
      else if (file.size === 0) errors.push(`${file.name}: el archivo está vacío.`);
      else valid.push(file);
    }
    this.files.set(valid);
    this.errors.set(errors);
    this.results.set([]);
  }

  upload(): void {
    this.session.upload();
  }

  clearFiles(input = this.fileInput): void {
    if (this.session.blocked()) return;
    if (input) input.value = '';
    this.session.clear();
  }

  sizeLabel(bytes: number): string {
    return bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`;
  }
  durationLabel(milliseconds: number): string {
    return milliseconds < 1000 ? `${milliseconds} ms` : `${(milliseconds / 1000).toFixed(1)} s`;
  }
  isError(state: string): boolean {
    return [
      'RECHAZADA',
      'SIN_FILAS_VALIDAS',
      'FALLO_MONGO',
      'PENDIENTE_NEO4J',
      'ARCHIVO_VACIO',
      'ARCHIVO_INVALIDO',
      'ARCHIVO_ILEGIBLE',
    ].includes(state);
  }

  stateLabel(state: string): string {
    const labels: Record<string, string> = {
      COMPLETA: 'Completada',
      YA_COMPLETA: 'Ya procesada',
      RECHAZADA: 'Rechazada',
      SIN_FILAS_VALIDAS: 'Sin filas válidas',
      FALLO_MONGO: 'Falló MongoDB',
      PENDIENTE_NEO4J: 'Pendiente Neo4j',
      ARCHIVO_VACIO: 'Archivo vacío',
      ARCHIVO_INVALIDO: 'Archivo inválido',
      ARCHIVO_ILEGIBLE: 'Error de lectura',
    };
    return labels[state] ?? state;
  }
}
