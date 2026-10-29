import { LoadHistory } from './load-history';
import { Component, inject, signal, viewChild } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

import { UploadFileResult, UploadResponse } from './upload.models';

@Component({
  selector: 'app-upload',
  imports: [LoadHistory],
  templateUrl: './upload.html',
})
export class Upload {
  private readonly api = inject(ApiService);
  readonly history = viewChild(LoadHistory);
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
    if (!this.files().length || this.loading()) return;
    const data = new FormData();
    this.files().forEach((file) => data.append('files', file, file.name));
    this.loading.set(true);
    this.errors.set([]);
    this.api
      .post<UploadResponse>('cargas', data)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          if (!response || !Array.isArray(response.archivos)) {
            this.errors.set([
              'El servidor devolvió un resultado de carga inválido. Puedes corregir la selección y reintentar.',
            ]);
            return;
          }
          this.results.set(response.archivos);
          this.history()?.open('cargas');
          if (response.mensaje) this.errors.set([response.mensaje]);
        },
        error: (error: unknown) =>
          this.errors.set([error instanceof ApiError ? error.message : 'No se pudo enviar la carga.']),
      });
  }

  clearFiles(input = this.fileInput): void {
    if (input) input.value = '';
    this.files.set([]);
    this.errors.set([]);
    this.results.set([]);
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
