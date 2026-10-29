import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';

@Component({template: '<p>Contenido de la sección</p>'})
class TestPage {}

describe('Menú principal con subsecciones', () => {
  beforeEach(() => TestBed.configureTestingModule({providers: [
    provideHttpClient(), provideHttpClientTesting(),
    provideRouter([
      {path: 'estadisticas', component: TestPage}, {path: 'grafos', component: TestPage},
      {path: 'carga', component: TestPage}, {path: 'busqueda', component: TestPage},
    ]),
  ]}));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('agrupa estadísticas y grafos, marca enlaces directos y navega desde el único sidebar', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/estadisticas?seccion=carreras');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/health').flush({status:'UP', mongodb:'UP', neo4j:'UP'});
    await fixture.whenStable();
    const root = fixture.nativeElement as HTMLElement;
    const current = () => root.querySelector('nav[aria-label="Navegación principal"] [aria-current="page"]') as HTMLAnchorElement;
    expect(root.querySelectorAll('aside')).toHaveLength(1);
    expect(root.querySelectorAll('.nav-subsections a')).toHaveLength(6);
    expect(current().textContent).toBe('Carreras');
    expect(current().getAttribute('href')).toBe('/estadisticas?seccion=carreras');
    expect(root.querySelector('main nav')).toBeNull();

    (root.querySelector('.nav-subsections a[href="/estadisticas?seccion=perfil"]') as HTMLAnchorElement).click();
    await fixture.whenStable();
    expect(router.url).toBe('/estadisticas?seccion=perfil');
    expect(current().textContent).toBe('Perfil y edades');

    await router.navigateByUrl('/grafos?seccion=resultados');
    await fixture.whenStable();
    expect(root.querySelectorAll('.nav-subsections a')).toHaveLength(5);
    expect(root.querySelector('[aria-label="Subsecciones de estadísticas"]')).toBeNull();
    expect(current().textContent).toBe('Resultados');
    expect(current().getAttribute('href')).toBe('/grafos?seccion=resultados');

    await router.navigateByUrl('/grafos?seccion=desconocida');
    await fixture.whenStable();
    expect(current().textContent).toBe('Explorar grafo');

    await router.navigateByUrl('/busqueda');
    await fixture.whenStable();
    expect(root.querySelector('.nav-subsections')).toBeNull();
    expect(current().textContent).toBe('Búsqueda');
  });
});
