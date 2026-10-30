import { CategoryFilters, FilteredRows } from '../../shared/category-filters';
import { DASHBOARD_FILTERS } from './dashboard.filters';
import { AdditionalAnalysis } from './components/additional-analysis';
import { PanoramaTables } from './components/panorama-tables';
import { ApprovalTables } from './components/aprobacion-tables';
import { CareerTables } from './components/carreras-tables';
import { ProfileTables } from './components/perfil-tables';
import { OriginTables } from './components/procedencia-tables';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DASHBOARD_SECTIONS } from './dashboard.sections';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

import { DashboardData, ChartDatum } from './dashboard.models';
import { queries } from './dashboard.queries';
import { isDashboard } from './dashboard.validation';
import { MetricChart } from './components/metric-chart';
@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    CategoryFilters,
    AdditionalAnalysis,
    MetricChart,
    PanoramaTables,
    ApprovalTables,
    CareerTables,
    ProfileTables,
    OriginTables,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly params = toSignal(this.route.queryParamMap);
  readonly sections = DASHBOARD_SECTIONS;
  readonly section = computed(
    () => this.sections.find((section) => section.id === this.params()?.get('seccion')) ?? this.sections[0],
  );
  readonly sectionQueries = computed(() => queries(this.section().id));
  readonly selectedQuery = computed(
    () =>
      this.sectionQueries().find((query) => query.id === this.params()?.get('consulta')) ??
      this.sectionQueries()[0],
  );
  readonly data = signal<DashboardData | null>(null);
  readonly filteredRows = signal<FilteredRows | null>(null);
  readonly filterConfig = computed(() => DASHBOARD_FILTERS[this.selectedQuery().mongoId]);
  readonly filterSource = computed(() => this.data()?.[this.filterConfig().source] ?? []);
  readonly visibleData = computed(() => {
    const data = this.data();
    const filtered = this.filteredRows();
    if (!data || filtered?.scope !== this.selectedQuery().mongoId) return data;
    return {...data, [this.filterConfig().source]: filtered.rows} as DashboardData;
  });
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }
  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.get<DashboardData>('estadisticas/dashboard').subscribe({
      next: (data) => {
        if (!isDashboard(data)) {
          this.error.set('El servidor devolvió estadísticas incompletas. Puedes reintentar la consulta.');
          this.loading.set(false);
          return;
        }
        this.data.set(data);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(error instanceof ApiError ? error.message : 'No se pudieron cargar los datos.');
        this.loading.set(false);
      },
    });
  }
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
  selectQuery(queryId: string): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { consulta: queryId },
      queryParamsHandling: 'merge',
    });
  }
  chartRows(data: DashboardData): ChartDatum[] {
    return this.selectedQuery().chartData(data);
  }
}
