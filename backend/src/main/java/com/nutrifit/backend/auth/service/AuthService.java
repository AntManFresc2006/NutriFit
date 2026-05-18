package com.nutrifit.backend.auth.service;

import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;

/**
 * Servicio de autenticación: registro, login y cierre de sesión.
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario y abre sesión automáticamente.
     * Valida unicidad del email (case-insensitive) y hash de contraseña con BCrypt.
     *
     * @param request datos de registro (nombre, email, contraseña mínimo 6 caracteres)
     * @return token de sesión válido 7 días e información del usuario creado
     * @throws BadRequestException si el email ya existe o los datos no cumplen validaciones
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Autentica un usuario por email y contraseña.
     * Compara la contraseña en texto plano con el hash almacenado usando BCrypt.
     *
     * @param request credenciales (email, contraseña)
     * @return token de sesión válido 7 días e información del usuario
     * @throws UnauthorizedException si el email no existe o la contraseña es incorrecta
     *         (mismo mensaje para ambos casos, previene enumeración de cuentas)
     */
    AuthResponse login(LoginRequest request);

    /**
     * Cierra sesión invalidando el token en la base de datos.
     *
     * @param token token de sesión a revocar
     * @throws BadRequestException si el token está vacío
     */
    void logout(String token);
}