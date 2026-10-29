import { Component, inject, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';

interface HistoryPage {
  items: Record<string, unknown>[];
  total: number;
  page: number;
  size: number;
}

/** Navega la trazabilidad guardada; una carga corresponde a un contenido distinto. */
@Component({
  selector: 'app-load-history',
  styles: [`.table-scroll { overflow-x: auto; } td { vertical-align: top; } dd { margin: 0 0 .5rem; }`],
  templateUrl: './load-history.html',
})
export class LoadHistory {
  private readonly api = inject(ApiService);
  private request = 0;
  readonly data = signal<HistoryPage | null>(null);
  readonly error = signal('');
  readonly loading = signal(false);
  readonly selected = signal<Record<string, unknown> | null>(null);
  path = 'cargas';
  page = 0;
  constructor() { this.load(); }

  open(path: string): void {
    if (path === 'cargas') this.selected.set(null);
    this.path = path;
    this.page = 0;
    this.load();
  }
  detail(row: Record<string, unknown>, kind: string): void {
    this.selected.set(row);
    this.open(`cargas/${row['sha256Archivo']}/detalle/${kind}`);
  }
  move(delta: number): void {
    this.page += delta;
    this.load();
  }
  text(value: unknown): string {
    return value === null || value === undefined ? 'Sin registro' : String(value);
  }
  entries(value: unknown): [string, unknown][] {
    return value && typeof value === 'object' ? Object.entries(value) : [];
  }
  sources(row: Record<string, unknown>): Record<string, unknown>[] {
    return Array.isArray(row['fuentes']) ? row['fuentes'] : [];
  }
  load(): void {
    const request = ++this.request;
    this.loading.set(true);
    this.error.set('');
    this.data.set(null);
    this.api.get<HistoryPage>(`${this.path}?page=${this.page}`).subscribe({
      next: data => {
        if (request !== this.request) return;
        this.data.set(data);
        this.loading.set(false);
      },
      error: () => {
        if (request !== this.request) return;
        this.error.set('No se pudo consultar la trazabilidad. Reintenta.');
        this.loading.set(false);
      },
    });
  }
}
