package com.nutrifit.backend.auth.security;

import com.nutrifit.backend.auth.model.Sesion;
import com.nutrifit.backend.auth.repository.SesionRepository;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * Interceptor Spring MVC que valida el token Bearer en cada petición protegida.
 *
 * <p>Comprueba que la cabecera {@code Authorization: Bearer <token>} está presente,
 * que la sesión existe en base de datos y que no ha expirado. Lanza
 * {@link UnauthorizedException} en caso contrario, lo que el
 * {@code GlobalExceptionHandler} convierte en una respuesta 401.</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SesionRepository sesionRepository;

    public AuthInterceptor(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token de autenticación requerido");
        }

        String token = authHeader.substring(7).trim();

        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Token inválido o sesión no encontrada"));

        if (sesion.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("La sesión ha expirado");
        }

        return true;
    }
}
