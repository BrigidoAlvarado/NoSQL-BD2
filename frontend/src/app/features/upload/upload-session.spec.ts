import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { apiErrorInterceptor } from '../../core/api-error.interceptor';
import { UploadSession } from './upload-session';
import { activeProgress, completedFile, idleProgress } from './upload.testing';

describe('Seguimiento de una carga', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [
    provideHttpClient(withInterceptors([apiErrorInterceptor])), provideHttpClientTesting(),
  ] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('recupera resultados después de recargar sin reenviar archivos ni restaurar la selección borrada', () => {
    const session = TestBed.inject(UploadSession);
    const http = TestBed.inject(HttpTestingController);
    const complete = { ...activeProgress, activa: false, estado: 'COMPLETA', archivos: [completedFile] };
    http.expectOne('/api/cargas/progreso').flush(complete);
    expect(session.results()).toEqual([completedFile]);
    expect(session.files()).toEqual([]);
    session.clear();
    session.refresh();
    http.expectOne('/api/cargas/progreso').flush(complete);
    expect(session.results()).toEqual([]);
    http.expectNone('/api/cargas');
  });

  it('recupera un lote activo y bloquea el envío desde otra pestaña del navegador', () => {
    const session = TestBed.inject(UploadSession);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/cargas/progreso').flush(activeProgress);
    session.files.set([new File(['uno'], 'uno.csv')]);
    session.upload();
    http.expectNone('/api/cargas');
    expect(session.blocked()).toBe(true);
    expect(session.progress()?.procesadas).toBe(500);
  });

  it('mantiene el bloqueo si se pierde HTTP y el backend sigue procesando', () => {
    const session = TestBed.inject(UploadSession);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/cargas/progreso').flush(idleProgress);
    session.files.set([new File(['uno'], 'uno.csv')]);
    session.upload();
    http.expectOne('/api/cargas').error(new ProgressEvent('error'));
    http.expectOne('/api/cargas/progreso').flush(activeProgress);
    expect(session.loading()).toBe(true);
    session.refresh();
    http.expectOne('/api/cargas/progreso').flush({}, { status: 503, statusText: 'Unavailable' });
    expect(session.blocked()).toBe(true);
    expect(session.trackingError()).toContain('no cancela');
    session.refresh();
    http.expectOne('/api/cargas/progreso').flush({ ...activeProgress, activa: false, estado: 'CON_ERRORES' });
    expect(session.blocked()).toBe(false);
  });

  it('rechaza un envío repetido y recupera el bloqueo del servidor después de un 409', () => {
    const session = TestBed.inject(UploadSession);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/cargas/progreso').flush(idleProgress);
    session.files.set([new File(['uno'], 'uno.csv')]);
    session.upload();
    session.upload();
    http.expectOne('/api/cargas').flush({ mensaje: 'Ya hay una carga en curso.' }, {
      status: 409, statusText: 'Conflict',
    });
    http.expectOne('/api/cargas/progreso').flush(activeProgress);
    expect(session.errors()[0]).toContain('Ya hay una carga');
    expect(session.loading()).toBe(true);
  });

  it('sondea cada tres segundos sin solapar consultas y se recupera de errores', async () => {
    vi.useFakeTimers();
    try {
      const session = TestBed.inject(UploadSession);
      const http = TestBed.inject(HttpTestingController);
      http.expectOne('/api/cargas/progreso').flush(activeProgress);
      await vi.advanceTimersByTimeAsync(3000);
      const poll = http.expectOne('/api/cargas/progreso');
      await vi.advanceTimersByTimeAsync(3000);
      http.expectNone('/api/cargas/progreso');
      poll.flush({}, { status: 503, statusText: 'Unavailable' });
      expect(session.loading()).toBe(true);
      await vi.advanceTimersByTimeAsync(3000);
      http.expectOne('/api/cargas/progreso').flush({ ...activeProgress, activa: false, estado: 'COMPLETA' });
      expect(session.loading()).toBe(false);
    } finally {
      vi.useRealTimers();
    }
  });
});
