package com.nutrifit.backend.auth.controller;

import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.auth.security.LoginRateLimiter;
import com.nutrifit.backend.auth.service.AuthService;
import com.nutrifit.backend.common.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticación: registro, login y logout.
 *
 * <p>Estos tres endpoints están excluidos del {@code AuthInterceptor} de forma
 * explícita en {@code WebMvcConfig}, por lo que no requieren token Bearer.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService authService, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Crea una cuenta nueva y abre sesión en el mismo paso.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return authService.register(request);
    }

    /**
     * Autentica al usuario y devuelve un token de sesión válido 7 días.
     *
     * <p>El mensaje de error es intencionadamente genérico para evitar enumerar cuentas.</p>
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        return authService.login(request);
    }

    /**
     * Invalida el token de la sesión activa eliminándolo de base de datos.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "").trim();
        authService.logout(token);
    }

    private void checkRateLimit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!rateLimiter.permitir(ip)) {
            throw new TooManyRequestsException("Demasiados intentos. Espera un minuto antes de volver a intentarlo.");
        }
    }
}
