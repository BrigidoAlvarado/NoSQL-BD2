import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, timeout } from 'rxjs';
import { ApiError } from './api-error.interceptor';
import { ApiService } from './api.service';

interface ConnectionHealth {
  status: 'UP' | 'DOWN';
  mongodb: 'UP' | 'DOWN';
  neo4j: 'UP' | 'DOWN';
}

function isHealth(value: unknown): value is ConnectionHealth {
  if (typeof value !== 'object' || value === null) return false;
  const health = value as Record<string, unknown>;
  const validState = (state: unknown) => state === 'UP' || state === 'DOWN';
  return validState(health['mongodb']) && validState(health['neo4j'])
    && health['status'] === (health['mongodb'] === 'UP' && health['neo4j'] === 'UP' ? 'UP' : 'DOWN');
}

@Component({
  selector: 'app-connection-status',
  template: `
    <section class="card" aria-labelledby="connection-heading" [attr.aria-busy]="loading()">
      <div class="status-heading">
        <h2 id="connection-heading">Estado de los servicios</h2>
        <button class="button secondary" type="button" [disabled]="loading()" (click)="check()">
          {{ loading() ? 'Comprobando…' : 'Comprobar conexión' }}
        </button>
      </div>
      <div role="status" aria-live="polite">
        @if (loading()) {
          <p>Consultando la disponibilidad de los servicios…</p>
        } @else if (health(); as result) {
          <p>{{ result.status === 'UP' ? 'Servicios disponibles.' : 'Hay servicios sin conexión. Puedes volver a comprobar.' }}</p>
          <ul class="services">
            <li>API: disponible</li>
            <li>MongoDB: {{ result.mongodb === 'UP' ? 'disponible' : 'sin conexión' }}</li>
            <li>Neo4j: {{ result.neo4j === 'UP' ? 'disponible' : 'sin conexión' }}</li>
          </ul>
        }
      </div>
      @if (error()) { <p class="error-message" role="alert">{{ error() }}</p> }
    </section>
  `,
  styles: `
    :host { display: block; margin-bottom: 1.5rem; }
    .status-heading { display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap; }
    h2 { margin: 0; }
    .services { display: flex; gap: .7rem 1.5rem; flex-wrap: wrap; list-style: none; padding: 0; margin-bottom: 0; }
    button:disabled { opacity: .65; cursor: wait; }
  `,
})
export class ConnectionStatus implements OnInit {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  readonly loading = signal(false);
  readonly health = signal<ConnectionHealth | null>(null);
  readonly error = signal('');

  ngOnInit(): void { this.check(); }

  check(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.health.set(null);
    this.error.set('');
    this.api.get<unknown>('health').pipe(
      timeout(45000),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (result) => {
        if (isHealth(result)) this.health.set(result);
        else this.error.set('No se pudo interpretar el estado de los servicios. Vuelve a comprobar.');
      },
      error: (error: unknown) => {
        if (error instanceof ApiError && error.response.status === 503 && isHealth(error.response.error)) {
          this.health.set(error.response.error);
        } else {
          this.error.set(error instanceof ApiError ? error.message
            : 'La comprobación no se completó a tiempo. Vuelve a intentarlo.');
        }
      },
    });
  }
}
