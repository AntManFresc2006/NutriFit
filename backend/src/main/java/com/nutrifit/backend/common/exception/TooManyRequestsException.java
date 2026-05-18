package com.nutrifit.backend.common.exception;

/**
 * Se lanza cuando el usuario ha excedido el límite de solicitudes permitidas.
 * HTTP status: 429 Too Many Requests.
 */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
