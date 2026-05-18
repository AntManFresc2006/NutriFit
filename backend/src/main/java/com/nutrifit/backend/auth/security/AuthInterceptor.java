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
 * Interceptor que valida tokens de autenticación en cada petición.
 * Valida presencia, validez y expiración; previene IDOR exponiendo el userId autenticado.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SesionRepository sesionRepository;

    public AuthInterceptor(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    /**
     * Valida que el token sea válido y no haya expirado. Busca el token en cookie (nf_session)
     * con fallback a header Authorization. Expone el userId en request.getAttribute("authenticatedUserId")
     * para prevención de IDOR en controladores.
     *
     * @param request  petición HTTP
     * @param response respuesta HTTP
     * @param handler  handler del endpoint
     * @return {@code true} si el token es válido y no ha expirado
     * @throws UnauthorizedException si no hay token, es inválido o ha expirado
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = null;

        // 1. Intenta leer cookie nf_session
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie c : cookies) {
                if ("nf_session".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        // 2. Fallback: Authorization header
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }
        }

        if (token == null) {
            throw new UnauthorizedException("Token de autenticación requerido");
        }

        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Token inválido o sesión no encontrada"));

        // La expiración se almacena en base de datos; la comprobamos aquí para no
        // aceptar tokens que ya caducaron aunque sigan presentes en sesiones
        if (sesion.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("La sesión ha expirada");
        }

        // Exponer el ID del usuario autenticado al controlador para verificar
        // que no intenta acceder a datos de otros usuarios (prevención de IDOR)
        request.setAttribute("authenticatedUserId", sesion.getUsuarioId());

        return true;
    }
}
