import { isHistoryPage } from './history.validation';

describe('Validación de páginas del historial', () => {
  it('distingue una página vacía de una respuesta incompleta', () => {
    expect(isHistoryPage({ items: [], total: 0, page: 0, size: 20 }, 'cargas')).toBe(true);
    expect(isHistoryPage({ items: [], total: 0 }, 'cargas')).toBe(false);
    expect(isHistoryPage({ items: [null], total: 1, page: 0, size: 20 }, 'cargas')).toBe(false);
  });
  it('rechaza un detalle sin causa de rechazo', () => {
    expect(isHistoryPage({ items: [{ fila: 1, valores: {} }], total: 1, page: 0, size: 20 }, 'cargas/hash/detalle/rechazos')).toBe(false);
  });
});
