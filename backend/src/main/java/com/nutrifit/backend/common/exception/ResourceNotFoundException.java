package com.nutrifit.backend.common.exception;

/**
 * Excepción especializada para casos donde un recurso requerido no existe en el sistema.
 * Diseñada para ser lanzada en capas de servicio/repositorio cuando fallan búsquedas por ID.
 * 
 * Al extender RuntimeException, permite un manejo flexible sin forzar declaraciones 'throws'.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Crea la excepción con un mensaje que debe identificar claramente:
     * - Qué tipo de recurso faltó (ej: "Usuario")
     * - El criterio de búsqueda fallido (ej: "con ID 123")
     * 
     * El mensaje será propagado hasta el GlobalExceptionHandler para convertirse
     * en la respuesta HTTP 404.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}