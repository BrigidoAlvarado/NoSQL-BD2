import { isAdditionalRows } from './additional.validation';

describe('Validación de consultas adicionales', () => {
  it('rechaza columnas ausentes, valores imposibles y respuestas que no son listas', () => {
    expect(isAdditionalRows({}, 'M21')).toBe(false);
    expect(isAdditionalRows([{ _id: { anio: 2024 } }], 'M21')).toBe(false);
    expect(isAdditionalRows([{ _id: { anio: 2024, materia: 'MATEMATICA' }, conFallo: 2, recuperados: 1, porcentaje: 101 }], 'M22')).toBe(false);
  });
  it('admite promedios nulos sin inventar cero', () => {
    expect(isAdditionalRows([{ _id: { anio: 2024 }, incluidos: 0, excluidos: 1, promedioDias: null, minimoDias: null, maximoDias: null }], 'M24')).toBe(true);
  });
});
