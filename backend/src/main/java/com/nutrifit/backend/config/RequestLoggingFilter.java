package com.nutrifit.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duracion = System.currentTimeMillis() - inicio;
            log.info("{} {} {} {}ms ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duracion,
                    request.getRemoteAddr());
        }
    }
}
