import { Pipe, PipeTransform } from '@angular/core';

// Equivalencia confirmada por el usuario para los CSV de 2022 y 2024.
const CAREER_NAMES: Record<string, string> = {
  'INGENIERIA EN CIENCIAS Y ': 'INGENIERIA EN CIENCIAS Y SISTEMAS',
  'INGENIERIA EN CIENCIAS Y': 'INGENIERIA EN CIENCIAS Y SISTEMAS',
};

/** Solo cambia la presentación; conserva claves, agrupaciones y datos recibidos. */
export function displayCareerName(value: string): string {
  return value.split(' · ').map(part => CAREER_NAMES[part] ?? part).join(' · ');
}

@Pipe({ name: 'careerName' })
export class CareerNamePipe implements PipeTransform {
  transform(value: string): string {
    return displayCareerName(value);
  }
}
