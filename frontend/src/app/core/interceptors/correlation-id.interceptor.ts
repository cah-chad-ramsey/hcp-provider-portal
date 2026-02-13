import { HttpInterceptorFn } from '@angular/common/http';

export const correlationIdInterceptor: HttpInterceptorFn = (req, next) => {
  // Generate correlation ID for request tracking
  const correlationId = generateCorrelationId();

  const modifiedReq = req.clone({
    setHeaders: {
      'X-Correlation-Id': correlationId
    }
  });

  return next(modifiedReq);
};

function generateCorrelationId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}
