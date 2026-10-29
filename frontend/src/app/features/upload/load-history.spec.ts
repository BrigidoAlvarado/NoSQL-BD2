import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LoadHistory } from './load-history';

describe('Historial persistente', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('pagina los rechazos conservando la carga seleccionada', () => {
    const fixture = TestBed.createComponent(LoadHistory);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/cargas?page=0').flush({ items: [], total: 0, page: 0, size: 20 });
    const row = { sha256Archivo: 'a'.repeat(64), nombreArchivo: 'datos.csv' };
    fixture.componentInstance.detail(row, 'rechazos');
    http.expectOne(`/api/cargas/${row.sha256Archivo}/detalle/rechazos?page=0`)
      .flush({ items: [{ fila: 1, codigo: 'ERROR', detalle: 'Dato inválido', valores: {} }], total: 121, page: 0, size: 20 });
    fixture.componentInstance.move(1);
    http.expectOne(`/api/cargas/${row.sha256Archivo}/detalle/rechazos?page=1`)
      .flush({ items: [{ fila: 21, codigo: 'ERROR', detalle: 'Dato inválido', valores: {} }], total: 121, page: 1, size: 20 });
    fixture.detectChanges();
    expect(fixture.componentInstance.selected()).toEqual(row);
    expect(fixture.nativeElement.textContent).toContain('Valores no disponibles');
    expect(fixture.nativeElement.textContent).toContain('Página 2');
  });

  it('descarta respuestas anteriores y permite reintentar una consulta fallida', () => {
    const fixture = TestBed.createComponent(LoadHistory);
    const http = TestBed.inject(HttpTestingController);
    const old = http.expectOne('/api/cargas?page=0');
    fixture.componentInstance.open('cargas');
    http.expectOne('/api/cargas?page=0').flush({ items: [], total: 0, page: 0, size: 20 });
    old.flush({ items: [{ nombreArchivo: 'antiguo.csv' }], total: 1, page: 0, size: 20 });
    expect(fixture.componentInstance.data()?.total).toBe(0);
    fixture.componentInstance.load();
    http.expectOne('/api/cargas?page=0').flush({}, { status: 503, statusText: 'Unavailable' });
    expect(fixture.componentInstance.error()).toContain('Reintenta');
    fixture.componentInstance.load();
    http.expectOne('/api/cargas?page=0').flush({ items: [], total: 0, page: 0, size: 20 });
    expect(fixture.componentInstance.error()).toBe('');
  });
});
