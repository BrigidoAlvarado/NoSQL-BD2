import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AdditionalAnalysis } from './additional-analysis';

describe('Consultas adicionales M21–M25', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('identifica las cinco consultas, muestra sus tablas y representa porcentajes independientes', () => {
    const fixture = TestBed.createComponent(AdditionalAnalysis);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/estadisticas/adicionales/M21').flush([]);
    for (const [id] of fixture.componentInstance.options) {
      fixture.componentInstance.select(id);
      const row = id === 'M25' ? { _id: { anio: 2024 }, porcentajeSinEdad: 100, porcentajeCarreraDesconocida: 100, porcentajeProcedenciaDesconocida: 100 }
        : { _id: { anio: 2024 }, porcentaje: 50, promedioDias: 10 };
      http.expectOne(`/api/estadisticas/adicionales/${id}`).flush([row]);
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('h2').textContent).toContain(id);
      expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(1);
      expect(fixture.componentInstance.chart()).toHaveLength(id === 'M25' ? 3 : 1);
    }
  });
});
