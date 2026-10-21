import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { apiErrorInterceptor } from './api-error.interceptor';
import { ConnectionStatus } from './connection-status';

describe('Estado de los servicios', () => {
  let fixture: ComponentFixture<ConnectionStatus>;
  let http: HttpTestingController;
  const content = () => fixture.nativeElement.textContent as string;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(withInterceptors([apiErrorInterceptor])), provideHttpClientTesting(),
    ] });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ConnectionStatus);
    fixture.detectChanges();
  });
  afterEach(() => { http.verify(); vi.useRealTimers(); });

  it('consulta la API y muestra ambas conexiones sin modificar datos', () => {
    const button = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(button.disabled).toBe(true);
    fixture.componentInstance.check();
    const request = http.expectOne('/api/health');
    expect(request.request.method).toBe('GET');
    request.flush({ status: 'UP', mongodb: 'UP', neo4j: 'UP' });
    fixture.detectChanges();
    expect(content()).toContain('Servicios disponibles.');
    expect(content()).toContain('MongoDB: disponible');
    expect(content()).toContain('Neo4j: disponible');
    expect(button.disabled).toBe(false);
  });

  it.each([
    { mongodb: 'UP', neo4j: 'DOWN', failed: 'Neo4j' },
    { mongodb: 'DOWN', neo4j: 'UP', failed: 'MongoDB' },
    { mongodb: 'DOWN', neo4j: 'DOWN', failed: 'MongoDB' },
  ])('conserva los estados del HTTP 503 y permite recuperar la conexión: $failed', (state) => {
    http.expectOne('/api/health').flush({ status: 'DOWN', mongodb: state.mongodb, neo4j: state.neo4j },
      { status: 503, statusText: 'Service Unavailable' });
    fixture.detectChanges();
    expect(content()).toContain(`${state.failed}: sin conexión`);
    expect(content()).toContain('API: disponible');
    (fixture.nativeElement.querySelector('button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(content()).not.toContain('sin conexión');
    http.expectOne('/api/health').flush({ status: 'UP', mongodb: 'UP', neo4j: 'UP' });
    fixture.detectChanges();
    expect(content()).toContain('Servicios disponibles.');
  });

  it('muestra un error de red sin afirmar que las bases estén disponibles', () => {
    http.expectOne('/api/health').error(new ProgressEvent('error'));
    fixture.detectChanges();
    expect(content()).toContain('No se pudo conectar con el servicio');
    expect(content()).not.toContain('MongoDB: disponible');
    expect(fixture.nativeElement.querySelector('[role=alert]')).not.toBeNull();
  });

  it('rechaza respuestas incompletas o inconsistentes', () => {
    http.expectOne('/api/health').flush({ status: 'UP', mongodb: 'DOWN', neo4j: 'UP' });
    fixture.detectChanges();
    expect(content()).toContain('No se pudo interpretar');
    expect(content()).not.toContain('Servicios disponibles.');
  });

  it('termina la espera y permite reintentar si la API no responde', () => {
    http.expectOne('/api/health').flush({ status: 'UP', mongodb: 'UP', neo4j: 'UP' });
    vi.useFakeTimers();
    fixture.componentInstance.check();
    const pending = http.expectOne('/api/health');
    vi.advanceTimersByTime(45000);
    fixture.detectChanges();
    expect(pending.cancelled).toBe(true);
    expect(content()).toContain('no se completó a tiempo');
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('cancela la petición al destruir la vista', () => {
    const request = http.expectOne('/api/health');
    fixture.destroy();
    expect(request.cancelled).toBe(true);
  });
});
