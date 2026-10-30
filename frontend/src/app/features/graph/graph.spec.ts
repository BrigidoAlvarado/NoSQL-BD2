import { vi } from 'vitest';
vi.mock('vis-network/standalone', () => import('./components/graph-network.mock'));
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { apiErrorInterceptor } from '../../core/api-error.interceptor';
import { GraphExplorer } from './graph';
import { GRAPH_SECTIONS } from './graph.sections';

describe('Selección de consultas Cypher por sección', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [
    provideHttpClient(withInterceptors([apiErrorInterceptor])), provideHttpClientTesting(), provideRouter([]),
  ] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  function create() {
    const fixture = TestBed.createComponent(GraphExplorer);
    TestBed.inject(HttpTestingController).expectOne('/api/grafos/opciones').flush({ carreras: [], departamentos: [] });
    return fixture;
  }
  function answer(id: string) {
    TestBed.inject(HttpTestingController).expectOne(`/api/grafos/consultas/${id}`).flush({
      id, titulo: id, descripcion: `Descripción ${id}`, columnas: ['materia'], filas: [{ materia: `FILA_${id}` }],
    });
  }
  it('ofrece los diez códigos y presenta solo la tabla elegida, conservando los resultados ya consultados', async () => {
    const router = TestBed.inject(Router);
    const http = TestBed.inject(HttpTestingController);
    await router.navigateByUrl('/?seccion=carreras');
    const fixture = create();
    answer('G01');
    for (const section of GRAPH_SECTIONS.filter(section => section.queries.length)) {
      await router.navigateByUrl(`/?seccion=${section.id}`);
      if (section.id !== 'carreras') answer(section.queries[0]);
      await fixture.whenStable();
      const select = fixture.nativeElement.querySelector('#graph-query') as HTMLSelectElement;
      expect(Array.from(select.options).map(option => option.value)).toEqual(section.queries);
      for (const option of Array.from(select.options)) expect(option.textContent).toContain(option.value + ' · ');
      for (const id of section.queries) {
        select.value = id;
        select.dispatchEvent(new Event('change'));
        await fixture.whenStable();
        if (id !== section.queries[0]) answer(id);
        await fixture.whenStable();
        expect(fixture.nativeElement.querySelectorAll('.graph-query-result')).toHaveLength(1);
        expect(fixture.nativeElement.querySelector('h3').textContent).toContain(id);
        expect(fixture.nativeElement.textContent).toContain(`FILA_${id}`);
        expect(router.url).toContain(`consulta=${id}`);
      }
      fixture.componentInstance.selectQuery(section.queries[0]);
      await fixture.whenStable();
      http.expectNone(request => request.url.startsWith('/api/grafos/consultas/'));
      expect(fixture.nativeElement.textContent).toContain(`FILA_${section.queries[0]}`);
    }
  });
  it('abre directamente G08, conserva el error de la selección y permite reintentar', async () => {
    await TestBed.inject(Router).navigateByUrl('/?seccion=procedencia&consulta=G08');
    const fixture = create();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/grafos/consultas/G08').flush({}, { status: 503, statusText: 'Unavailable' });
    http.expectNone('/api/grafos/consultas/G03');
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#graph-query').value).toBe('G08');
    const button = fixture.nativeElement.querySelector('.graph-query-result button') as HTMLButtonElement;
    expect(button.textContent).toContain('Reintentar');
    button.click(); answer('G08');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('FILA_G08');
  });
  it('descarta un código de otra sección y no muestra respuestas de una selección anterior', async () => {
    const router = TestBed.inject(Router);
    const http = TestBed.inject(HttpTestingController);
    await router.navigateByUrl('/?seccion=resultados&consulta=G08');
    const fixture = create();
    const pending = http.expectOne('/api/grafos/consultas/G02');
    fixture.componentInstance.selectQuery('G05');
    await fixture.whenStable();
    answer('G05');
    pending.flush({ id: 'G02', titulo: 'Anterior', descripcion: 'ANTERIOR', columnas: ['materia'], filas: [] });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('FILA_G05');
    expect(fixture.nativeElement.textContent).not.toContain('ANTERIOR');
  });
  it('envía categorías como parámetros y permite limpiar aun cuando no hay resultados', async () => {
    await TestBed.inject(Router).navigateByUrl('/?seccion=resultados');
    const fixture = create();
    answer('G02');
    await fixture.whenStable();
    const select = fixture.nativeElement.querySelector('app-category-filters select') as HTMLSelectElement;
    select.value = 'FILA_G02';
    select.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    TestBed.inject(HttpTestingController).expectOne(request => request.url === '/api/grafos/consultas/G02'
      && request.params.get('materia') === 'FILA_G02').flush({
        id: 'G02', titulo: 'G02', descripcion: 'Filtrada', columnas: ['materia'], filas: [],
      });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('app-category-filters')).not.toBeNull();
    expect((fixture.nativeElement.querySelector('app-category-filters select') as HTMLSelectElement).value).toBe('FILA_G02');
    (fixture.nativeElement.querySelector('app-category-filters button') as HTMLButtonElement).click();
    await fixture.whenStable();
    answer('G02');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('FILA_G02');
  });

  it('mantiene el grafo visual separado y permite reintentarlo', async () => {
    const fixture = create();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(request => request.url === '/api/grafos' && request.params.get('limite') === '10')
      .flush({}, { status: 503, statusText: 'Unavailable' });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#graph-query')).toBeNull();
    fixture.componentInstance.loadGraph();
    http.expectOne(request => request.url === '/api/grafos').flush({ evaluacionesIncluidas: 0, nodos: [], relaciones: [] });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No hay nodos');
  });
});
