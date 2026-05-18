package com.nutrifit.backend.common.exception;

/**
 * Se lanza cuando un recurso solicitado no existe en la base de datos.
 * HTTP status: 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}