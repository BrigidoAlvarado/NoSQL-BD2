import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiService } from './api.service';
import { ApiError, apiErrorInterceptor } from './api-error.interceptor';

describe('Acceso a la API', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [
    provideHttpClient(withInterceptors([apiErrorInterceptor])), provideHttpClientTesting(),
  ] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('usa la base /api y devuelve la respuesta del backend', () => {
    let result: unknown;
    TestBed.inject(ApiService).get('/ejemplo').subscribe((value) => result = value);
    TestBed.inject(HttpTestingController).expectOne('/api/ejemplo').flush({ total: 3 });
    expect(result).toEqual({ total: 3 });
  });

  it('expone un error legible de conexión conservando el error HTTP', () => {
    let error: ApiError | undefined;
    TestBed.inject(ApiService).get('ejemplo').subscribe({ error: (value: ApiError) => error = value });
    TestBed.inject(HttpTestingController).expectOne('/api/ejemplo').error(new ProgressEvent('error'));
    expect(error).toBeInstanceOf(ApiError);
    expect(error?.message).toContain('No se pudo conectar');
    expect(error?.response.status).toBe(0);
  });

  it('muestra el mensaje controlado cuando MongoDB no está disponible', () => {
    let error: ApiError | undefined;
    TestBed.inject(ApiService).get('estadisticas/dashboard').subscribe({ error: (value: ApiError) => error = value });
    TestBed.inject(HttpTestingController).expectOne('/api/estadisticas/dashboard').flush(
      { codigo: 'MONGODB_NO_DISPONIBLE', mensaje: 'detalle que debe normalizarse' }, { status: 503, statusText: 'Service Unavailable' });
    expect(error?.message).toContain('MongoDB no está disponible');
    expect(error?.response.status).toBe(503);
  });
});
