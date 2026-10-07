import { TestBed } from '@angular/core/testing';
import { Upload } from './upload';

describe('Selección de CSV', () => {
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
    (fixture.nativeElement.querySelector('button') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.componentInstance.files()).toEqual([]);
    expect(fixture.componentInstance.errors()).toEqual([]);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });
});
