import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

export class ApiError extends Error {
  constructor(readonly response: HttpErrorResponse) {
    const body = typeof response.error === 'object' && response.error !== null ? response.error as { message?: unknown; mensaje?: unknown; codigo?: unknown } : {};
    const backendMessage = typeof body.mensaje === 'string' ? body.mensaje : typeof body.message === 'string' ? body.message : undefined;
    const engineMessage = body.codigo === 'MONGODB_NO_DISPONIBLE'
      ? 'MongoDB no está disponible. Conserva tu trabajo y vuelve a intentarlo cuando el servicio se recupere.'
      : body.codigo === 'NEO4J_NO_DISPONIBLE'
        ? 'Neo4j no está disponible. Puedes seguir usando las funciones que no requieren grafos y reintentar después.'
        : undefined;
    super(engineMessage ?? backendMessage ?? (response.status === 0
      ? 'No se pudo conectar con el servicio. Intenta nuevamente más tarde.'
      : response.status >= 500
        ? 'El servicio no pudo completar la operación. Intenta nuevamente más tarde.'
        : 'No se pudo completar la solicitud. Revisa los datos e intenta nuevamente.'));
  }
}

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith(`${environment.apiBaseUrl}/`)) return next(request);
  return next(request).pipe(catchError((error: unknown) =>
    throwError(() => error instanceof HttpErrorResponse ? new ApiError(error) : error),
  ));
};
