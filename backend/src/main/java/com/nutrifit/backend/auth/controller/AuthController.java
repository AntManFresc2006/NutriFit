package com.nutrifit.backend.auth.controller;

import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.auth.service.AuthService;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Crea una cuenta nueva y abre sesión en el mismo paso.
     *
     * <p>Si el email ya está en uso se devuelve 400 para no revelar qué
     * cuentas existen sin necesidad de un endpoint de comprobación aparte.</p>
     *
     * @param request datos del nuevo usuario (nombre, email, contraseña ≥ 6 chars)
     * @return token de sesión e información básica del usuario recién creado
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Autentica al usuario y devuelve un token de sesión válido 7 días.
     *
     * <p>El mensaje de error es intencionadamente genérico ("Credenciales inválidas")
     * tanto cuando el email no existe como cuando la contraseña es incorrecta,
     * para evitar enumerar cuentas registradas.</p>
     *
     * @param request email y contraseña en texto plano
     * @return token de sesión e información básica del usuario
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Invalida el token de la sesión activa eliminándolo de base de datos.
     *
     * <p>El token se extrae aquí de la cabecera para no repetir ese parsing
     * dentro del servicio, que solo necesita la cadena limpia.</p>
     *
     * @param authorizationHeader cabecera {@code Authorization: Bearer <token>}
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "").trim();
        authService.logout(token);
    }
}