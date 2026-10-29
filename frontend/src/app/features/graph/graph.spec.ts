import { vi } from 'vitest';
vi.mock('vis-network/standalone', () => import('./components/graph-network.mock'));
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { apiErrorInterceptor } from '../../core/api-error.interceptor';
import { GraphExplorer } from './graph';

describe('Explorador de grafos por subsecciones', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiErrorInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        ApiService,
      ],
    }),
  );
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('abre solo el grafo, conserva filtros y muestra todas las consultas en su tema', async () => {
    const fixture = TestBed.createComponent(GraphExplorer);
    TestBed.inject(HttpTestingController).expectOne('/api/grafos/opciones').flush({ carreras: [], departamentos: [] });
    const http = TestBed.inject(HttpTestingController);
    http
      .expectOne((request) => request.url === '/api/grafos' && request.params.get('limite') === '10')
      .flush({ evaluacionesIncluidas: 0, nodos: [], relaciones: [] });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No hay nodos');
    expect(fixture.nativeElement.textContent).not.toContain('Actualizar');
    fixture.componentInstance.anio = '2022';
    fixture.componentInstance.loadGraph();
    http
      .expectOne((request) => request.url === '/api/grafos' && request.params.get('anio') === '2022')
      .flush({ evaluacionesIncluidas: 0, nodos: [], relaciones: [] });
    for (const [section, ids] of [
      ['carreras', ['G01']],
      ['trayectorias', ['G04', 'G07', 'G10']],
      ['resultados', ['G02', 'G05', 'G09']],
      ['procedencia', ['G03', 'G06', 'G08']],
    ] as const) {
      await TestBed.inject(Router).navigateByUrl('/?seccion=' + section);
      for (const id of ids) {
        http.expectOne('/api/grafos/consultas/' + id).flush({
          id,
          titulo: 'Consulta',
          descripcion: 'Prueba',
          columnas: ['carrera', 'evaluaciones'],
          filas: [{ carrera: 'CARRERA_' + id, evaluaciones: 3 }],
        });
      }
      await fixture.whenStable();
      expect(fixture.nativeElement.querySelectorAll('.graph-query-result')).toHaveLength(ids.length);
      expect(fixture.nativeElement.querySelector('.graph-canvas')).toBeNull();
      expect(fixture.nativeElement.querySelector('.graph-filters')).toBeNull();
      for (const id of ids) expect(fixture.nativeElement.textContent).toContain('CARRERA_' + id);
    }
    await TestBed.inject(Router).navigateByUrl('/?seccion=carreras');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('CARRERA_G01');
    expect(fixture.nativeElement.textContent).not.toContain('CARRERA_G03');
    await TestBed.inject(Router).navigateByUrl('/?seccion=grafo');
    await fixture.whenStable();
    expect(fixture.componentInstance.anio).toBe('2022');
    expect(fixture.nativeElement.textContent).not.toContain('CARRERA_G01');
  });

  it('abre un enlace directo sin cargar el grafo y recupera solo la consulta que falló', async () => {
    const router = TestBed.inject(Router);
    const http = TestBed.inject(HttpTestingController);
    await router.navigateByUrl('/?seccion=trayectorias');
    const fixture = TestBed.createComponent(GraphExplorer);
    TestBed.inject(HttpTestingController).expectOne('/api/grafos/opciones').flush({ carreras: [], departamentos: [] });
    http
      .expectOne('/api/grafos/consultas/G04')
      .flush({ codigo: 'NEO4J_NO_DISPONIBLE' }, { status: 503, statusText: 'Service Unavailable' });
    for (const id of ['G07', 'G10'])
      http.expectOne('/api/grafos/consultas/' + id).flush({
        id,
        titulo: 'Trayectorias',
        descripcion: 'Prueba',
        columnas: ['aspirantes'],
        filas: [],
      });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Neo4j no está disponible');
    expect(fixture.nativeElement.textContent).toContain('La consulta no encontró resultados');
    const retry = fixture.nativeElement.querySelector('.graph-query-result button') as HTMLButtonElement;
    expect(retry.textContent).toContain('Reintentar');
    retry.click();
    http.expectOne('/api/grafos/consultas/G04').flush({
      id: 'G04',
      titulo: 'Trayectorias',
      descripcion: 'Recuperada',
      columnas: ['carrera'],
      filas: [{ carrera: 'SISTEMAS' }],
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('SISTEMAS');
    expect(fixture.nativeElement.textContent).not.toContain('Neo4j no está disponible');
  });

  it('mantiene una respuesta tardía fuera del tema seleccionado', async () => {
    const http = TestBed.inject(HttpTestingController);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/?seccion=procedencia');
    const fixture = TestBed.createComponent(GraphExplorer);
    TestBed.inject(HttpTestingController).expectOne('/api/grafos/opciones').flush({ carreras: [], departamentos: [] });
    const pending = http.expectOne('/api/grafos/consultas/G03');
    const institution = http.expectOne('/api/grafos/consultas/G06');
    const municipality = http.expectOne('/api/grafos/consultas/G08');
    await router.navigateByUrl('/?seccion=resultados');
    pending.flush({
      id: 'G03',
      titulo: 'Procedencia',
      descripcion: 'Departamento tardío',
      columnas: ['departamento'],
      filas: [{ departamento: 'GUATEMALA' }],
    });
    institution.flush({
      id: 'G06',
      titulo: 'Institución tardía',
      descripcion: 'Prueba',
      columnas: ['tipoInstitucion'],
      filas: [],
    });
    municipality.flush({
      id: 'G08',
      titulo: 'Municipio tardío',
      descripcion: 'Prueba',
      columnas: ['municipio'],
      filas: [],
    });
    for (const id of ['G02', 'G05', 'G09'])
      http.expectOne('/api/grafos/consultas/' + id).flush({
        id,
        titulo: 'Resultados',
        descripcion: 'Prueba',
        columnas: ['materia'],
        filas: [],
      });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).not.toContain('Departamento tardío');
    expect(fixture.nativeElement.textContent).not.toContain('GUATEMALA');
  });

  it('permite reintentar el grafo tras una caída', async () => {
    const fixture = TestBed.createComponent(GraphExplorer);
    TestBed.inject(HttpTestingController).expectOne('/api/grafos/opciones').flush({ carreras: [], departamentos: [] });
    const http = TestBed.inject(HttpTestingController);
    http
      .expectOne((request) => request.url === '/api/grafos')
      .flush({ codigo: 'NEO4J_NO_DISPONIBLE' }, { status: 503, statusText: 'Service Unavailable' });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Neo4j no está disponible');
    const retry = fixture.nativeElement.querySelector('.graph-filters .button') as HTMLButtonElement;
    expect(retry.disabled).toBe(false);
    retry.click();
    http
      .expectOne((request) => request.url === '/api/grafos')
      .flush({
        evaluacionesIncluidas: 1,
        nodos: [
          {
            id: 'a',
            etiqueta: 'Aspirante',
            titulo: 'Aspirante A',
            propiedades: {},
          },
        ],
        relaciones: [],
      });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.graph-canvas')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });
});
