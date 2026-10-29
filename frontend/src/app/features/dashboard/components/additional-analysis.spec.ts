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
      const fixtures: Record<string, unknown> = {
        M21: { _id: { anio: 2024, materia: 'MATEMATICA', oportunidad: 1 }, total: 2, aprobadas: 1, noAprobadas: 1, porcentaje: 50 },
        M22: { _id: { anio: 2024, materia: 'MATEMATICA' }, conFallo: 2, recuperados: 1, porcentaje: 50 },
        M23: { _id: { anio: 2024, materias: 2 }, aspirantes: 1, totalCohorte: 2, porcentaje: 50 },
        M24: { _id: { anio: 2024 }, incluidos: 1, excluidos: 1, promedioDias: 10, minimoDias: 10, maximoDias: 10 },
        M25: { _id: { anio: 2024 }, total: 1, sinEdad: 1, carreraDesconocida: 1, procedenciaDesconocida: 1, porcentajeSinEdad: 100, porcentajeCarreraDesconocida: 100, porcentajeProcedenciaDesconocida: 100 },
      };
      const row = fixtures[id];
      http.expectOne(`/api/estadisticas/adicionales/${id}`).flush([row]);
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('h2').textContent).toContain(id);
      expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(1);
      expect(fixture.componentInstance.chart()).toHaveLength(id === 'M25' ? 3 : 1);
    }
  });
});
