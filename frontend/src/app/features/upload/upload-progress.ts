import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { UploadSession } from './upload-session';

/** Presenta mensajes de progreso; las excepciones internas no se envían al navegador. */
@Component({
  selector: 'app-upload-progress',
  imports: [DatePipe],
  templateUrl: './upload-progress.html',
  styleUrl: './upload-progress.css',
})
export class UploadProgressView {
  readonly session = inject(UploadSession);
}
