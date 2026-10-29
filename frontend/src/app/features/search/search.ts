import { CareerNamePipe } from '../../shared/career-name.pipe';
import { ApplicantDetailView } from './components/applicant-detail';
import { HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ApiError } from '../../core/api-error.interceptor';
import { ApiService } from '../../core/api.service';

import { SearchData, ApplicantDetail } from './search.models';

@Component({
  selector: 'app-search',
  imports: [CareerNamePipe, ApplicantDetailView],
  templateUrl: './search.html',
})
export class Search {
  private readonly api = inject(ApiService);
  readonly results = signal<SearchData | null>(null);
  readonly applicant = signal<ApplicantDetail | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  criterio = 'aspirante';
  termino = '';

  search(): void {
    const term = this.termino.trim();
    this.applicant.set(null);
    this.error.set(null);
    if (!term) {
      this.results.set(null);
      this.error.set('Ingresa un término para realizar la búsqueda.');
      return;
    }
    this.loading.set(true);
    const params = new HttpParams().set('criterio', this.criterio).set('termino', term).set('limite', '50');
    this.api.get<SearchData>('busqueda', params).subscribe({
      next: (data) => {
        if (!isSearch(data)) {
          this.error.set('El servidor devolvió resultados de búsqueda inválidos. Puedes reintentar.');
          this.loading.set(false);
          return;
        }
        this.results.set(data);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(error instanceof ApiError ? error.message : 'No se pudo completar la búsqueda.');
        this.loading.set(false);
      },
    });
  }
  loadApplicant(id: string): void {
    this.error.set(null);
    this.loading.set(true);
    this.api.get<ApplicantDetail>(`busqueda/aspirantes/${encodeURIComponent(id)}`).subscribe({
      next: (data) => {
        if (!isApplicant(data)) {
          this.error.set('El servidor devolvió un historial inválido. Puedes reintentar.');
          this.loading.set(false);
          return;
        }
        this.applicant.set(data);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(error instanceof ApiError ? error.message : 'No se pudo consultar el historial.');
        this.loading.set(false);
      },
    });
  }
  inputValue(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }
  placeholder(): string {
    return (
      (
        {
          aspirante: 'Pega el correlativo anónimo completo',
          carrera: 'Ej. SISTEMAS',
          institucion: 'Ej. PRIVADO',
          materia: 'Ej. MATEMATICA',
          municipio: 'Ej. MIXCO',
          departamento: 'Ej. GUATEMALA',
        } as Record<string, string>
      )[this.criterio] ?? ''
    );
  }
}

function isSearch(value: unknown): value is SearchData {
  return (
    typeof value === 'object' &&
    value !== null &&
    Array.isArray((value as SearchData).resultados) &&
    typeof (value as SearchData).totalDevuelto === 'number'
  );
}
function isApplicant(value: unknown): value is ApplicantDetail {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as ApplicantDetail).aspiranteId === 'string' &&
    Array.isArray((value as ApplicantDetail).evaluaciones)
  );
}
