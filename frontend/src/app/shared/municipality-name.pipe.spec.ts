import { displayMunicipality } from './municipality-name.pipe';

describe('Presentación del municipio junto al departamento', () => {
  it('conserva las zonas al quitar el departamento repetido', () => {
    expect(displayMunicipality('GUATEMALA - CIUDAD', 'Guatemala')).toBe('CIUDAD');
    expect(displayMunicipality('GUATEMALA - CIUDAD CAPITAL ZONA 1', 'GUATEMALA'))
      .toBe('CIUDAD CAPITAL ZONA 1');
  });

  it('conserva nombres completos, desconocidos y prefijos ajenos', () => {
    for (const value of ['GUATEMALA', 'MIXCO', 'SIN REGISTRO', 'GUATEMALA -']) {
      expect(displayMunicipality(value, 'GUATEMALA')).toBe(value);
    }
    expect(displayMunicipality('GUATEMALA - CIUDAD', 'SACATEPEQUEZ'))
      .toBe('GUATEMALA - CIUDAD');
    expect(displayMunicipality('GUATEMALA - CIUDAD', '')).toBe('GUATEMALA - CIUDAD');
  });
});
