import { Component, computed, effect, input, output, signal, untracked } from '@angular/core';
import { displayCareerName } from './career-name.pipe';

export interface CategoryField { key: string; label: string; }
export interface FilteredRows { scope: string; rows: readonly unknown[]; }
export function categoryValue(row: unknown, key: string): string {
  let value = row;
  for (const part of key.split('.')) {
    value = value && typeof value === 'object' ? (value as Record<string, unknown>)[part] : undefined;
  }
  return value === null || value === undefined ? '' : String(value);
}
export function filterCategories(rows: readonly unknown[], values: Record<string, string>): readonly unknown[] {
  return rows.filter(row => Object.entries(values).every(([key, value]) => !value || categoryValue(row, key) === value));
}

/** Filtra únicamente filas recibidas; conserva las métricas y las identidades originales. */
@Component({
  selector: 'app-category-filters',
  template: `
    <fieldset class="category-filters">
      <legend>Filtrar resultados</legend>
      @for (field of fields(); track field.key) {
        <label>{{ field.label }}
          <select [value]="values()[field.key] || ''" (change)="choose(field.key, $any($event.target).value)">
            <option value="">Todos</option>
            @for (option of options(field.key); track option) {
              <option [value]="option" [selected]="option === values()[field.key]">{{ label(option) }}</option>
            }
          </select>
        </label>
      }
      <button class="button secondary" type="button" (click)="clear()">Limpiar filtros</button>
    </fieldset>
    <p class="hint" role="status" [hidden]="server()">{{ visible().length }} de {{ rows().length }} filas. Se filtra el resultado recibido; los porcentajes conservan su base original. Los rankings solo incluyen las posiciones devueltas.</p>
    @if (!server() && rows().length && !visible().length) { <p class="empty-result">No hay filas que coincidan con los filtros.</p> }
  `,
  styles: [`.category-filters { display:flex; flex-wrap:wrap; align-items:end; gap:12px; margin:16px 0; padding:16px; border:1px solid #d7e1ed; border-radius:10px; }
    legend { font-size:.82rem; font-weight:600; } label { display:grid; gap:6px; flex:1 1 180px; min-width:0; font-size:.8rem; }`],
})
export class CategoryFilters {
  readonly rows = input.required<readonly unknown[]>();
  readonly fields = input.required<readonly CategoryField[]>();
  readonly scope = input.required<string>();
  readonly server = input(false);
  readonly selectionChange = output<{scope: string; values: Record<string, string>}>();
  readonly filtered = output<FilteredRows>();
  private readonly selection = signal<{scope: string; values: Record<string, string>}>({scope: '', values: {}});
  readonly values = computed(() => this.selection().scope === this.scope() ? this.selection().values : {});
  readonly visible = computed(() => filterCategories(this.rows(), this.values()));
  constructor() {
    effect(() => { const event = {scope: this.scope(), values: this.values()};
      if (this.server()) untracked(() => this.selectionChange.emit(event));
      else { const result = {scope: this.scope(), rows: this.visible()}; untracked(() => this.filtered.emit(result)); } });
  }
  options(key: string): string[] {
    return [...new Set(this.rows().map(row => categoryValue(row, key)).filter(Boolean))]
      .sort((a, b) => a.localeCompare(b, 'es', { numeric: true }));
  }
  label(value: string): string {
    const display = displayCareerName(value);
    return display === value ? value : `${display} (${value.trim()})`;
  }
  choose(key: string, value: string): void {
    this.selection.set({scope: this.scope(), values: {...this.values(), [key]: value}});
  }
  clear(): void { this.selection.set({scope: this.scope(), values: {}}); }
}
