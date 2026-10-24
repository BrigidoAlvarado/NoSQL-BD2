import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiService } from '../../core/api.service';
import { Dashboard } from './dashboard';

describe('Dashboard de estadísticas', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), ApiService] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('muestra indicadores y las seis consultas recibidas del backend', async () => {
    const fixture = TestBed.createComponent(Dashboard);
    const request = TestBed.inject(HttpTestingController).expectOne('/api/estadisticas/dashboard');
    request.flush({
      resumen: { evaluaciones: 10, aspirantesUnicos: 7, aprobadas: 4, porcentajeAprobacion: 40 },
      aspirantesPorInstitucion: [{ etiqueta: 'PRIVADO', cantidad: 5 }],
      aprobadasPorMateria: [{ etiqueta: 'MATEMATICA', cantidad: 4 }],
      aprobacionPorMateria: [{ etiqueta: 'MATEMATICA', aprobadas: 4, evaluaciones: 10, porcentajeAprobacion: 40 }],
      aspirantesPorSexoInstitucion: [{ primeraEtiqueta: 'F', segundaEtiqueta: 'PRIVADO', cantidad: 3 }],
      aprobacionPorInstitucion: [{ etiqueta: 'PRIVADO', aprobadas: 4, evaluaciones: 10, porcentajeAprobacion: 40 }],
      resultadosPorAnio: [{ anioIngreso: 2022, aprobadas: 4, noAprobadas: 6, evaluaciones: 10, porcentajeAprobacion: 40 }],
      aprobadosPorCarreraAnio: [], edadPorCarrera: [], resumenCarreras: [], edadAprobadosPorCarreraInstitucion: [],
      aprobadosPorMunicipioCarrera: [], evaluacionesPublicasPorMesMateria: [], topCarrerasJuveniles: [], aprobacionPorEdad: [],
      oportunidadesPorMateria: [], noAprobaronPrimerIntentoPorCarrera: [], topMunicipios: [], carrerasMasDemandadasPorDepartamento: [],
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Aspirantes únicos');
    expect(fixture.nativeElement.textContent).toContain('MATEMATICA');
    expect(fixture.nativeElement.textContent).toContain('2022');
  });
});
