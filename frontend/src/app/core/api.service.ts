import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  get<T>(path: string, params?: HttpParams) {
    return this.http.get<T>(this.url(path), { params });
  }

  post<T>(path: string, body: unknown) {
    return this.http.post<T>(this.url(path), body);
  }

  private url(path: string): string {
    return `${environment.apiBaseUrl.replace(/\/$/, '')}/${path.replace(/^\/+/, '')}`;
  }
}
