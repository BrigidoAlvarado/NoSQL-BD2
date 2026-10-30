import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { activeProgress, completedFile, idleProgress } from './features/upload/upload.testing';
import { Upload } from './features/upload/upload';
import { UploadSession } from './features/upload/upload-session';

@Component({template: '<p>Contenido de la sección</p>'})
class TestPage {}

describe('Menú principal con subsecciones', () => {
  beforeEach(() => TestBed.configureTestingModule({providers: [
    provideHttpClient(), provideHttpClientTesting(),
    provideRouter([
      {path: 'estadisticas', component: TestPage}, {path: 'grafos', component: TestPage},
      {path: 'carga', component: Upload}, {path: 'busqueda', component: TestPage},
    ]),
  ]}));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('conserva la solicitud al salir de carga y recupera los mensajes al regresar', async () => {
    const router = TestBed.inject(Router);
    const http = TestBed.inject(HttpTestingController);
    await router.navigateByUrl('/carga');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    http.expectOne('/api/health').flush({ status: 'UP', mongodb: 'UP', neo4j: 'UP' });
    http.expectOne('/api/cargas/progreso').flush(idleProgress);
    http.expectOne('/api/cargas?page=0').flush({ items: [], total: 0, page: 0, size: 20 });
    const session = TestBed.inject(UploadSession);
    session.files.set([new File(['uno'], 'uno.csv')]);
    session.upload();
    const post = http.expectOne('/api/cargas');
    await router.navigateByUrl('/busqueda');
    fixture.detectChanges();
    expect(post.cancelled).toBe(false);
    expect(fixture.nativeElement.querySelector('app-upload-status').textContent).toContain('Carga en curso');
    session.refresh();
    http.expectOne('/api/cargas/progreso').flush(activeProgress);
    await router.navigateByUrl('/carga');
    fixture.detectChanges();
    http.expectOne('/api/cargas?page=0').flush({ items: [], total: 0, page: 0, size: 20 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-upload-progress').textContent)
      .toContain('Neo4j: 500 de 1000 evaluaciones procesadas.');
    expect(fixture.nativeElement.querySelector('#csv-files').disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('uno.csv');
    session.upload();
    http.expectNone('/api/cargas');
    await router.navigateByUrl('/busqueda');
    post.flush({ archivos: [completedFile], mensaje: null });
    http.expectOne('/api/cargas/progreso').flush({
      ...activeProgress, activa: false, estado: 'COMPLETA', archivos: [completedFile],
    });
    await router.navigateByUrl('/carga');
    fixture.detectChanges();
    http.expectOne('/api/cargas?page=0').flush({ items: [], total: 0, page: 0, size: 20 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-upload-status').textContent.trim()).toBe('');
    expect(fixture.nativeElement.querySelector('.results-card').textContent).toContain('Carga sincronizada.');
    expect(session.loading()).toBe(false);
  });

  it('agrupa estadísticas y grafos, marca enlaces directos y navega desde el único sidebar', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/estadisticas?seccion=carreras');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/cargas/progreso').flush(idleProgress);
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
