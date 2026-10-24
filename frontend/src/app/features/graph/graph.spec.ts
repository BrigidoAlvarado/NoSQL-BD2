import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiService } from '../../core/api.service';
import { GraphExplorer } from './graph';

describe('Explorador de grafos', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), ApiService] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('consulta el grafo, aplica un filtro y muestra una consulta Cypher', async () => {
    const fixture = TestBed.createComponent(GraphExplorer); const http = TestBed.inject(HttpTestingController);
    http.expectOne(request => request.url === '/api/grafos' && request.params.get('limite') === '40').flush({ evaluacionesIncluidas: 1, nodos: [{ id: 'Aspirante:a', etiqueta: 'Aspirante', titulo: 'Aspirante a', propiedades: {} }], relaciones: [] });
    http.expectOne('/api/grafos/consultas/G01').flush({ id: 'G01', titulo: 'Demanda', descripcion: 'Prueba', columnas: ['carrera', 'evaluaciones'], filas: [{ carrera: 'SISTEMAS', evaluaciones: 3 }] });
    await fixture.whenStable(); expect(fixture.nativeElement.textContent).toContain('SISTEMAS');
    fixture.componentInstance.anio = '2022'; fixture.componentInstance.loadGraph();
    http.expectOne(request => request.url === '/api/grafos' && request.params.get('anio') === '2022').flush({ evaluacionesIncluidas: 0, nodos: [], relaciones: [] });
    await fixture.whenStable(); expect(fixture.nativeElement.textContent).toContain('No hay nodos');
  });
});
