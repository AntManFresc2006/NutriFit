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
 * Interceptor Spring MVC que protege todos los endpoints bajo {@code /api/**}.
 *
 * <p>Comprueba que la cabecera {@code Authorization: Bearer <token>} está presente,
 * que la sesión existe en base de datos y que no ha expirado. Lanza
 * {@link UnauthorizedException} en cualquier caso de fallo; el
 * {@link com.nutrifit.backend.common.exception.GlobalExceptionHandler}
 * se encarga de convertirla en una respuesta 401 con cuerpo JSON uniforme.</p>
 *
 * <p>Los endpoints de login y registro están excluidos de este interceptor
 * en {@code WebMvcConfig#addInterceptors}.</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SesionRepository sesionRepository;

    public AuthInterceptor(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    /**
     * Valida el token Bearer antes de que la petición llegue al controlador.
     *
     * <p>Se lanza {@link UnauthorizedException} (no se devuelve false) para que el
     * {@code GlobalExceptionHandler} construya la respuesta JSON de error en lugar
     * de que Spring devuelva una respuesta vacía con código 200.</p>
     *
     * @return {@code true} siempre que el token sea válido y la sesión esté vigente
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token de autenticación requerido");
        }

        // substring(7) elimina el prefijo "Bearer " sin depender de replace para no fallar
        // si el token contiene accidentalmente esa cadena más adelante
        String token = authHeader.substring(7).trim();

        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Token inválido o sesión no encontrada"));

        // La expiración se almacena en base de datos; la comprobamos aquí para no
        // aceptar tokens que ya caducaron aunque sigan presentes en sesiones
        if (sesion.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("La sesión ha expirado");
        }

        return true;
    }
}
