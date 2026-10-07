import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

export class ApiError extends Error {
  constructor(readonly response: HttpErrorResponse) {
    super(response.status === 0
      ? 'No se pudo conectar con el servicio. Intenta nuevamente más tarde.'
      : response.status >= 500
        ? 'El servicio no pudo completar la operación. Intenta nuevamente más tarde.'
        : 'No se pudo completar la solicitud. Revisa los datos e intenta nuevamente.');
  }
}

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith(`${environment.apiBaseUrl}/`)) return next(request);
  return next(request).pipe(catchError((error: unknown) =>
    throwError(() => error instanceof HttpErrorResponse ? new ApiError(error) : error),
  ));
};
