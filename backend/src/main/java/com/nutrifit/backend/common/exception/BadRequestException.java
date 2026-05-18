package com.nutrifit.backend.common.exception;

/**
 * Se lanza cuando una solicitud es inválida o contiene datos malformados.
 * HTTP status: 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}