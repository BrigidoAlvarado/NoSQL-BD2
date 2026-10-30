import { TestBed } from '@angular/core/testing';
import { GraphQueryChart } from './graph-query-chart';
import { QueryData } from '../graph.models';

describe('Gráfica asociada a la tabla Cypher', () => {
  it('actualiza las barras con la respuesta filtrada y presenta el estado vacío', async () => {
    const fixture = TestBed.createComponent(GraphQueryChart);
    const original: QueryData = {id: 'G02', titulo: '', descripcion: '', columnas: [], filas: [
      {materia: 'MATEMATICA', resultado: 'APROBADO', evaluaciones: 5},
      {materia: 'COMPUTACION', resultado: 'NO APROBADO', evaluaciones: 9},
    ]};
    fixture.componentRef.setInput('result', original);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelectorAll('.chart-bars > div')).toHaveLength(2);
    fixture.componentRef.setInput('result', {...original, filas: [original.filas[0]]});
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelectorAll('.chart-bars > div')).toHaveLength(1);
    expect(fixture.nativeElement.textContent).not.toContain('COMPUTACION');
    fixture.componentRef.setInput('result', {...original, filas: []});
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No hay datos para graficar');
  });
});
