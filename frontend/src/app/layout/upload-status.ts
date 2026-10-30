import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UploadSession } from '../features/upload/upload-session';

/** Mantiene visible la carga desde cualquier sección de la aplicación. */
@Component({
  selector: 'app-upload-status',
  imports: [RouterLink],
  template: `
    @if (session.loading()) {
      <p class="processing" role="status">
        Carga en curso.
        @if (session.progress(); as progress) {
          Archivo {{ progress.archivoActual }} de {{ progress.totalArchivos }}:
          {{ progress.nombreArchivo }}.
        }
        <a routerLink="/carga">Ver avance</a>
        @if (session.trackingError()) { El seguimiento no está disponible; se reintentará. }
      </p>
    }
  `,
})
export class UploadStatus {
  readonly session = inject(UploadSession);
}
