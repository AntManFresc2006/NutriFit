package com.nutrifit.backend.common.exception;

/**
 * Excepción utilizada para errores de petición inválida.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}