import { CareerNamePipe, displayCareerName } from './career-name.pipe';

describe('Nombre visible de carrera', () => {
  it('completa solo la equivalencia confirmada y conserva las demás categorías', () => {
    const pipe = new CareerNamePipe();
    expect(pipe.transform('INGENIERIA EN CIENCIAS Y ')).toBe('INGENIERIA EN CIENCIAS Y SISTEMAS');
    expect(pipe.transform('INGENIERIA EN CIENCIAS Y SISTEMAS')).toBe('INGENIERIA EN CIENCIAS Y SISTEMAS');
    expect(pipe.transform('INGENIERIA MECANICA ')).toBe('INGENIERIA MECANICA ');
    expect(pipe.transform('NINGUNO')).toBe('NINGUNO');
  });

  it('conserva año e institución de las etiquetas compuestas de gráficas', () => {
    expect(displayCareerName('INGENIERIA EN CIENCIAS Y  · 2024'))
      .toBe('INGENIERIA EN CIENCIAS Y SISTEMAS · 2024');
  });
});
