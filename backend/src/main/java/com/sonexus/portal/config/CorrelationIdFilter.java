package com.sonexus.portal.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that ensures every request has a correlation ID for tracing.
 * The correlation ID is:
 * 1. Read from X-Correlation-ID request header if present
 * 2. Generated as a UUID if not present
 * 3. Added to MDC for logging
 * 4. Returned in X-Correlation-ID response header
 */
@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Get or generate correlation ID
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Add to MDC for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // Add to response header
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            log.debug("Processing request with correlation ID: {}", correlationId);

            chain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
