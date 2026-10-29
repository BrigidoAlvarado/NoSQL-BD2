import { Component, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/api.service';
import { MetricChart } from './metric-chart';

/** Selecciona una consulta adicional y presenta sus resultados y unidades. */
@Component({
  selector: 'app-additional-analysis',
  imports: [MetricChart],
  template: `
    <section class="card">
      <label>Consulta adicional <select [value]="id" (change)="select($any($event.target).value)">
        @for (option of options; track option[0]) { <option [value]="option[0]">{{ option[0] }} · {{ option[1] }}</option> }
      </select></label>
      <h2>{{ id }} · {{ title() }}</h2><p>{{ descriptions[id] }}</p>
      @if (loading()) { <p role="status">Consultando…</p> }
      @if (error()) { <p role="alert">{{ error() }}</p><button (click)="load()">Reintentar</button> }
      <div style="overflow-x:auto"><table><thead><tr>
        @for (column of columns(); track column) { <th>{{ column }}</th> }
      </tr></thead><tbody>
        @for (row of rows(); track $index) { <tr>@for (column of columns(); track column) { <td>{{ display(row[column]) }}</td> }</tr> }
      </tbody></table></div>
      @if (!loading() && !error() && !rows().length) { <p>Sin resultados.</p> }
      <h3>{{ id }} · Lectura visual</h3>
      <app-metric-chart [rows]="chart()" kind="bar" [title]="title()" />
    </section>
  `,
})
export class AdditionalAnalysis {
  private readonly api = inject(ApiService);
  private request = 0;
  id = 'M21';
  readonly rows = signal<Record<string, unknown>[]>([]);
  readonly error = signal('');
  readonly loading = signal(false);
  readonly options = [
    ['M21', 'Aprobación por oportunidad y materia'],
    ['M22', 'Recuperación después de un fallo'],
    ['M23', 'Cantidad de materias por aspirante'],
    ['M24', 'Intervalo entre asignaciones'],
    ['M25', 'Calidad de información por cohorte'],
  ];
  readonly descriptions: Record<string, string> = {
    M21: 'Porcentaje sobre evaluaciones de cada cohorte, materia y oportunidad.',
    M22: 'Recuperados: aprobación en una oportunidad mayor que un fallo de la misma materia y cohorte. Porcentaje sobre aspirantes con algún fallo.',
    M23: 'Materias distintas por aspirante y cohorte; porcentaje sobre aspirantes de la cohorte. Los reintentos no aumentan las materias.',
    M24: 'Días entre primera y última asignación. Solo incluye personas con dos fechas distintas; no mide tiempo de preparación ni fecha de examen.',
    M25: 'Cada condición se mide sobre evaluaciones de la cohorte. Las condiciones pueden superponerse y sus porcentajes no deben sumarse.',
  };
  constructor() { this.load(); }
  title(): string { return this.options.find(option => option[0] === this.id)![1]; }
  select(id: string): void { this.id = id; this.load(); }
  columns(): string[] { return Object.keys(this.rows()[0] ?? {}); }
  display(value: unknown): string {
    if (value === null || value === undefined) return 'Sin datos';
    if (typeof value === 'object') return Object.entries(value).map(([key, v]) => `${key}: ${v}`).join(' · ');
    return typeof value === 'number' && !Number.isInteger(value) ? value.toFixed(2) : String(value);
  }
  chart(): { label: string; value: number }[] {
    return this.rows().flatMap(row => {
      const label = this.display(row['_id']);
      const fields = this.id === 'M25' ? ['porcentajeSinEdad', 'porcentajeCarreraDesconocida', 'porcentajeProcedenciaDesconocida']
        : [this.id === 'M24' ? 'promedioDias' : 'porcentaje'];
      return fields.filter(field => typeof row[field] === 'number').map(field => ({ label: `${label} · ${field}`, value: row[field] as number }));
    });
  }
  load(): void {
    const request = ++this.request;
    this.loading.set(true); this.rows.set([]); this.error.set('');
    this.api.get<Record<string, unknown>[]>(`estadisticas/adicionales/${this.id}`).subscribe({
      next: rows => { if (request === this.request) { this.rows.set(rows); this.loading.set(false); } },
      error: () => { if (request === this.request) { this.error.set('No se pudo ejecutar la consulta.'); this.loading.set(false); } },
    });
  }
}
