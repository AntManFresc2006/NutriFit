package com.nutrifit.backend.common.exception;

/**
 * Excepción utilizada cuando las credenciales no son válidas
 * o el acceso no está autorizado.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}