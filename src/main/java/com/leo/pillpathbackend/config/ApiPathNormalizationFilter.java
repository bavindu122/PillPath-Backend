package com.leo.pillpathbackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Normalizes accidental double version prefix like "/api/v1/v1/..." to "/api/v1/...".
 * This runs before Spring Security so matchers and controllers see the normalized path.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiPathNormalizationFilter implements Filter {

    private static final String DOUBLE_V1 = "/api/v1/v1/";
    private static final String SINGLE_V1 = "/api/v1/";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            String uri = req.getRequestURI();
            if (uri != null && uri.startsWith(DOUBLE_V1)) {
                String normalized = SINGLE_V1 + uri.substring(DOUBLE_V1.length());
                // Forward internally to the normalized path so downstream filters/controllers see it
                RequestDispatcher dispatcher = request.getRequestDispatcher(normalized);
                dispatcher.forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

