import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from './app.routes';

describe('Navegación inicial', () => {
  it('abre el dashboard por defecto y muestra cada sección mediante sus rutas', async () => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });
    const harness = await RouterTestingHarness.create();
    for (const [path, heading] of [
      ['/', 'Estadísticas de admisión'],
      ['/carga', 'Carga de datos'],
      ['/busqueda', 'Búsqueda de aspirantes'],
      ['/grafos', 'Explorador de grafos'],
      ['/ruta-inexistente', 'Estadísticas de admisión'],
    ]) {
      await harness.navigateByUrl(path);
      expect(harness.routeNativeElement?.querySelector('h1')?.textContent).toBe(heading);
    }
  });
});
