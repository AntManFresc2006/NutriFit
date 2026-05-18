package com.nutrifit.backend.common.exception;

/**
 * Se lanza cuando una solicitud requiere autenticación o esta es inválida.
 * HTTP status: 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}