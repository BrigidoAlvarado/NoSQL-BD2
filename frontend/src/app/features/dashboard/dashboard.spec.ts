import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { CountTable, Dashboard } from './dashboard';

describe('Dashboard de estadísticas', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), ApiService] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('segmenta las 18 consultas por tema y muestra una consulta con su gráfica sin volver a consultar la API', async () => {
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
    expect(fixture.nativeElement.textContent).toContain('Aspirantes por tipo de institución');
    expect(fixture.nativeElement.textContent).not.toContain('Evaluaciones aprobadas por materia');
    const ids: string[] = [];
    for (const [section, count] of [['panorama', 3], ['aprobacion', 4], ['carreras', 4], ['perfil', 3], ['procedencia', 4]] as const) {
      await TestBed.inject(Router).navigateByUrl(`/?seccion=${section}`);
      await fixture.whenStable();
      expect(fixture.nativeElement.querySelectorAll('.query-picker option')).toHaveLength(count);
      expect(fixture.nativeElement.querySelectorAll('.dashboard-results > section')).toHaveLength(2);
      expect(fixture.nativeElement.querySelector('.analysis-heading h2').textContent).toContain(fixture.componentInstance.section().label);
      ids.push(fixture.nativeElement.querySelector('.data-table-card .hint').textContent!.match(/M\d\d/)![0]);
    }
    expect(ids).toHaveLength(5);
    expect(new Set(ids).size).toBe(5);
    expect(ids).not.toContain('M11');
    expect(ids).not.toContain('M15');
    await TestBed.inject(Router).navigateByUrl('/?seccion=aprobacion&consulta=aprobadas-materia');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('MATEMATICA');
    await TestBed.inject(Router).navigateByUrl('/?seccion=inexistente');
    await fixture.whenStable();
    expect(fixture.componentInstance.section().id).toBe('panorama');
  });

  it('compara cantidades con barras sin alterar los valores ni dividir entre cero', () => {
    const fixture = TestBed.createComponent(CountTable);
    fixture.componentRef.setInput('rows', [{ etiqueta: 'PUBLICO', cantidad: 10 }, { etiqueta: 'PRIVADO', cantidad: 5 }]);
    fixture.detectChanges();
    const bars = fixture.nativeElement.querySelectorAll('.comparison-track > span') as NodeListOf<HTMLElement>;
    expect(bars[0].style.width).toBe('100%');
    expect(bars[1].style.width).toBe('50%');
    fixture.componentRef.setInput('rows', [{ etiqueta: 'PUBLICO', cantidad: 0 }]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.comparison-track > span').style.width).toBe('0%');
    fixture.componentRef.setInput('rows', []);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No hay datos para esta consulta.');
  });
});
