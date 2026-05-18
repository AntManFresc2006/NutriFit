package com.nutrifit.backend.auth.controller;

import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.auth.security.LoginRateLimiter;
import com.nutrifit.backend.auth.service.AuthService;
import com.nutrifit.backend.common.exception.TooManyRequestsException;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación: registro, login y logout con manejo de cookies de sesión.
 */
@Tag(name = "Autenticación", description = "Endpoints de registro, login y logout")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    /**
     * Inyecta las dependencias de autenticación y rate limiting.
     *
     * @param authService    servicio de autenticación
     * @param rateLimiter    limitador de intentos de login
     */
    public AuthController(AuthService authService, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Registra un nuevo usuario. Verifica el rate limit y abre sesión automáticamente.
     *
     * @param request      datos de registro (nombre, email, contraseña)
     * @param httpRequest  petición HTTP (para extraer IP)
     * @param response     respuesta HTTP (para establecer cookie)
     * @return token de sesión e información del usuario
     * @throws TooManyRequestsException si se superan los intentos permitidos
     * @throws BadRequestException si el email ya existe o los datos son inválidos
     */
    @Operation(summary = "Registrar nuevo usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "429", description = "Demasiados intentos")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        checkRateLimit(httpRequest);
        AuthResponse authResponse = authService.register(request);
        setAuthCookie(response, authResponse.getToken());
        return authResponse;
    }

    /**
     * Inicia sesión con email y contraseña. Verifica el rate limit y establece cookie de sesión.
     *
     * @param request      credenciales (email, contraseña)
     * @param httpRequest  petición HTTP (para extraer IP)
     * @param response     respuesta HTTP (para establecer cookie)
     * @return token de sesión e información del usuario
     * @throws TooManyRequestsException si se superan los intentos permitidos
     * @throws UnauthorizedException si las credenciales son inválidas
     */
    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sesión iniciada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "429", description = "Demasiados intentos")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        checkRateLimit(httpRequest);
        AuthResponse authResponse = authService.login(request);
        setAuthCookie(response, authResponse.getToken());
        return authResponse;
    }

    /**
     * Cierra sesión: invalida el token y limpia la cookie. Busca el token en cookie (prioridad) o header.
     *
     * @param request  petición HTTP (para extraer token de cookie o header)
     * @param response respuesta HTTP (para limpiar cookie)
     * @throws UnauthorizedException si no se encuentra token válido
     */
    @Operation(summary = "Cerrar sesión")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sesión cerrada exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = null;

        // 1. Try to read nf_session cookie
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

        authService.logout(token);
        clearAuthCookie(response);
    }

    private void checkRateLimit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!rateLimiter.permitir(ip)) {
            throw new TooManyRequestsException("Demasiados intentos. Espera un minuto antes de volver a intentarlo.");
        }
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        String cookie = "nf_session=" + token
            + "; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=86400";
        response.addHeader("Set-Cookie", cookie);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        String cookie = "nf_session=; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=0";
        response.addHeader("Set-Cookie", cookie);
    }
}
