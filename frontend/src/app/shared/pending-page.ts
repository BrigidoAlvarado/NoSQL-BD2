import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-pending-page',
  template: `
    <p class="eyebrow">{{ content.eyebrow }}</p>
    <h1>{{ content.heading }}</h1>
    <p class="page-description">{{ content.description }}</p>
    <section class="card empty-state">
      <h2>Próximamente</h2><p>{{ content.message }}</p>
      <div class="tags" aria-label="Criterios previstos">
        @for (criterion of content.criteria; track criterion) { <span class="tag">{{ criterion }}</span> }
      </div>
    </section>
  `,
})
export class PendingPage {
  readonly content = inject(ActivatedRoute).snapshot.data as {
    eyebrow: string; heading: string; description: string; message: string; criteria: string[];
  };
}
