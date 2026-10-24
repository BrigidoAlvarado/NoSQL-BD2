import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiService } from '../../core/api.service';
import { Search } from './search';

describe('Buscador de aspirantes', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), ApiService] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('busca coincidencias y muestra las evaluaciones distintas de un aspirante', async () => {
    const fixture = TestBed.createComponent(Search); const component = fixture.componentInstance; const http = TestBed.inject(HttpTestingController);
    component.criterio = 'materia'; component.termino = 'MATEMATICA'; component.search();
    http.expectOne(request => request.url === '/api/busqueda' && request.params.get('criterio') === 'materia').flush({ criterio: 'materia', termino: 'MATEMATICA', totalDevuelto: 1, resultados: [{ aspiranteId: 'abc', fechaAsignacion: '2022-01-01', materia: 'MATEMATICA', oportunidad: 1, anioIngreso: 2022, aprobacion: 'APROBADO', carreraObjetivo: 'SISTEMAS', tipoInstitucion: 'PRIVADO', municipio: 'GUATEMALA', departamento: 'GUATEMALA' }] });
    component.loadApplicant('abc');
    http.expectOne('/api/busqueda/aspirantes/abc').flush({ aspiranteId: 'abc', totalEvaluaciones: 2, evaluacionesAprobadas: 1, evaluaciones: [{ fechaAsignacion: '2022-01-01', sexo: 'F', anioNacimiento: 2004, edadIngreso: 18, materia: 'MATEMATICA', oportunidad: 1, anioIngreso: 2022, aprobacion: 'APROBADO', carreraObjetivo: 'SISTEMAS', tipoInstitucion: 'PRIVADO', municipio: 'GUATEMALA', departamento: 'GUATEMALA' }, { fechaAsignacion: '2022-01-02', sexo: 'F', anioNacimiento: 2004, edadIngreso: 18, materia: 'FISICA', oportunidad: 2, anioIngreso: 2022, aprobacion: 'NO APROBADO', carreraObjetivo: 'SISTEMAS', tipoInstitucion: 'PRIVADO', municipio: 'GUATEMALA', departamento: 'GUATEMALA' }] });
    await fixture.whenStable(); expect(fixture.nativeElement.textContent).toContain('FISICA'); expect(fixture.nativeElement.textContent).toContain('1 aprobadas de 2');
  });
  it('informa si el término está vacío', () => {
    const fixture = TestBed.createComponent(Search); fixture.componentInstance.search(); fixture.detectChanges(); expect(fixture.nativeElement.textContent).toContain('Ingresa un término');
  });
});
