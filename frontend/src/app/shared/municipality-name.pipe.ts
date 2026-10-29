import { Pipe, PipeTransform } from '@angular/core';

/** Usar únicamente cuando el departamento también esté visible. */
export function displayMunicipality(value: string, department: string): string {
  const separator = value.indexOf('-');
  if (separator < 0 || !department.trim()) return value;
  const prefix = value.slice(0, separator).trim();
  const name = value.slice(separator + 1).trim();
  return prefix.toUpperCase() === department.trim().toUpperCase() && name
    ? name
    : value;
}

@Pipe({ name: 'municipalityName' })
export class MunicipalityNamePipe implements PipeTransform {
  transform(value: string, department: string): string {
    return displayMunicipality(value, department);
  }
}
