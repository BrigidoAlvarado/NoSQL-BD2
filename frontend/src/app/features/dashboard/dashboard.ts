import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <p class="eyebrow">PANORAMA GENERAL</p>
    <h1>Estadísticas de admisión</h1>
    <p class="page-description">Evaluaciones, resultados y tendencias de aspirantes a la Facultad de Ingeniería.</p>
    <div class="metrics">
      @for (label of metrics; track label) {
        <section class="card metric"><h2>{{ label }}</h2><p class="metric-value">—</p><span>Sin datos disponibles</span></section>
      }
    </div>
    <section class="card empty-state">
      <span class="empty-symbol" aria-hidden="true">↗</span>
      <h2>Tu análisis comienza con los datos</h2>
      <p>Prepara los archivos CSV de pruebas específicas para consultar las estadísticas de admisión.</p>
      <a class="button" routerLink="/carga">Preparar archivos CSV</a>
      <p class="hint">Los resultados estarán disponibles al integrar el servicio de estadísticas.</p>
    </section>
  `,
})
export class Dashboard {
  readonly metrics = ['Aspirantes únicos', 'Evaluaciones', 'Porcentaje de aprobación'];
}
