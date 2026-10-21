import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiService } from '../../core/api.service';
import { Upload } from './upload';

describe('Selección de CSV', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), ApiService] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('acepta varios CSV, rechaza archivos vacíos y otras extensiones, y limpia la selección', async () => {
    const fixture = TestBed.createComponent(Upload);
    await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    Object.defineProperty(input, 'files', { value: [
      new File(['materia\nMATEMATICA'], 'evaluaciones.csv'),
      new File(['materia\nFISICA'], 'otro.CSV'),
      new File([], 'vacio.csv'),
      new File(['texto'], 'notas.txt'),
    ] });
    input.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    expect(fixture.componentInstance.files().map((file) => file.name)).toEqual(['evaluaciones.csv', 'otro.CSV']);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('vacio.csv');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('notas.txt');
    (fixture.nativeElement.querySelector('.button.secondary') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.componentInstance.files()).toEqual([]);
    expect(fixture.componentInstance.errors()).toEqual([]);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });

  it('envía varios CSV y muestra el resultado y las causas de rechazo por archivo', async () => {
    const fixture = TestBed.createComponent(Upload);
    await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    Object.defineProperty(input, 'files', { value: [new File(['uno'], 'uno.csv'), new File(['dos'], 'dos.csv')] });
    input.dispatchEvent(new Event('change'));
    fixture.componentInstance.upload();
    const request = TestBed.inject(HttpTestingController).expectOne('/api/cargas');
    expect(request.request.method).toBe('POST');
    expect((request.request.body as FormData).getAll('files')).toHaveLength(2);
    request.flush({ archivos: [{ nombreArchivo: 'uno.csv', sha256Archivo: 'abc', estado: 'COMPLETA', registrosLeidos: 4,
      registrosProcesados: 3, registrosRechazados: 1, duracionMilisegundos: 250, reutilizada: false, detalle: 'Carga sincronizada.',
      rechazos: [{ line: 3, code: 'FECHA_INVALIDA', detail: 'fecha inválida' }] }], mensaje: null });
    await fixture.whenStable();
    expect(fixture.componentInstance.results()).toHaveLength(1);
    expect(fixture.nativeElement.textContent).toContain('Completada');
    expect(fixture.nativeElement.textContent).toContain('FECHA_INVALIDA');
  });
});
