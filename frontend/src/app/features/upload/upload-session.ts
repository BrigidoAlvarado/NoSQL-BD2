import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, exhaustMap, interval, of, timeout } from 'rxjs';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';
import { UploadFileResult, UploadProgress, UploadResponse } from './upload.models';

/** Conserva la solicitud y su estado aunque Angular destruya la página de carga. */
@Injectable({ providedIn: 'root' })
export class UploadSession {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly posting = signal(false);
  readonly checking = signal(true);
  readonly files = signal<File[]>([]);
  readonly errors = signal<string[]>([]);
  readonly results = signal<UploadFileResult[]>([]);
  readonly progress = signal<UploadProgress | null>(null);
  readonly trackingError = signal('');
  readonly loading = computed(() => this.posting() || this.progress()?.activa === true);
  readonly blocked = computed(() => this.loading() || this.checking());

  constructor() {
    this.refresh();
    // El seguimiento pertenece a la aplicación, no a una ruta. No se solapan sondeos.
    interval(3000).pipe(
      exhaustMap(() => this.readProgress()),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(progress => this.receive(progress));
  }

  refresh(): void {
    this.readProgress().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(progress => this.receive(progress));
  }

  upload(): void {
    if (!this.files().length || this.blocked()) return;
    const data = new FormData();
    this.files().forEach(file => data.append('files', file, file.name));
    this.posting.set(true);
    this.errors.set([]);
    this.results.set([]);
    this.progress.set(null);
    // Esta suscripción sigue viva al navegar; salir de la vista no cancela el ETL.
    this.api.post<UploadResponse>('cargas', data)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          if (!response || !Array.isArray(response.archivos)) {
            this.errors.set(['El servidor devolvió un resultado de carga inválido. Revisa el historial.']);
          } else {
            this.results.set(response.archivos);
            if (response.mensaje) this.errors.set([response.mensaje]);
          }
          this.posting.set(false);
          this.refresh();
        },
        error: (error: unknown) => {
          this.errors.set([error instanceof ApiError ? error.message : 'No se pudo enviar la carga.']);
          this.posting.set(false);
          // Perder HTTP no demuestra que el servidor haya cancelado el procesamiento.
          this.checking.set(true);
          this.refresh();
        },
      });
  }

  clear(): void {
    if (this.blocked()) return;
    this.files.set([]);
    this.errors.set([]);
    this.results.set([]);
  }

  private readProgress() {
    return this.api.get<UploadProgress>('cargas/progreso').pipe(
      timeout(10000),
      catchError(() => of(null)),
    );
  }

  private receive(progress: UploadProgress | null): void {
    this.checking.set(false);
    if (!progress || typeof progress.activa !== 'boolean' || !Array.isArray(progress.eventos)
        || !Array.isArray(progress.archivos)) {
      this.trackingError.set('No se pudo consultar el avance. Se reintentará automáticamente; esto no cancela la carga.');
      return;
    }
    this.trackingError.set('');
    // Un lote anterior puede aparecer mientras el nuevo POST todavía está enviándose.
    if (this.posting() && !progress.activa && !this.progress()?.id) return;
    const previous = this.progress();
    this.progress.set(progress);
    const changed = previous?.id !== progress.id
      || previous?.archivos.length !== progress.archivos.length
      || (previous?.activa && !progress.activa);
    if (!this.posting() && changed && progress.archivos.length) this.results.set(progress.archivos);
  }
}
