import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, PRIMARY_OUTLET, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { ConnectionStatus } from './layout/connection-status';
import { UploadStatus } from './layout/upload-status';
import { DASHBOARD_SECTIONS } from './features/dashboard/dashboard.sections';
import { GRAPH_SECTIONS } from './features/graph/graph.sections';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, ConnectionStatus, UploadStatus],
  templateUrl: './app.html',
})
export class App {
  private readonly router = inject(Router);
  private readonly url = toSignal(this.router.events.pipe(
    filter((event): event is NavigationEnd => event instanceof NavigationEnd),
    map(event => event.urlAfterRedirects),
    startWith(this.router.url),
  ));
  private readonly location = computed(() => this.router.parseUrl(this.url() ?? '/'));
  readonly area = computed(() => this.location().root.children[PRIMARY_OUTLET]?.segments[0]?.path ?? 'estadisticas');
  readonly dashboardSections = DASHBOARD_SECTIONS;
  readonly graphSections = GRAPH_SECTIONS;
  readonly activeSection = computed(() => {
    const sections = this.area() === 'grafos' ? this.graphSections : this.dashboardSections;
    return sections.find(section => section.id === this.location().queryParams['seccion'])?.id ?? sections[0].id;
  });
}
