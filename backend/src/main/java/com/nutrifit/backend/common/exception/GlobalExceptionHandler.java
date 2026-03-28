package com.nutrifit.backend.common.exception;

import com.nutrifit.backend.common.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

/**
 * Punto central de traducción de excepciones a respuestas HTTP en NutriFit.
 *
 * <p>Sin esta clase, Spring devolvería stacktraces en texto plano o respuestas
 * vacías con códigos de error, lo que obligaría al cliente JavaFX a parsear
 * formatos variables. Aquí todas las excepciones se convierten a {@link ApiError}
 * con estructura JSON consistente.</p>
 *
 * <p>Orden de prioridad de los handlers: Spring elige el más específico, por lo que
 * {@code handleGeneric} solo actúa si ningún handler más concreto coincide.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * El recurso solicitado no existe en base de datos.
     *
     * <p>Esta excepción la lanzan los servicios cuando un {@code findById} no encuentra
     * el registro; aquí la convertimos en 404 en lugar de dejar que el 500 genérico
     * oculte lo que realmente pasó.</p>
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiError error = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Un DTO anotado con {@code @Valid} no superó la validación de Jakarta.
     *
     * <p>Se extrae solo el primer error de campo para no abrumar al cliente con
     * una lista entera. El mensaje viene de la anotación del DTO (p.ej. "El nombre es obligatorio"),
     * no de aquí.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(org.springframework.context.MessageSourceResolvable::getDefaultMessage)
                .orElse("Error de validación");

        ApiError error = buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Petición malformada: JSON inválido, enum desconocido o violación de constraint de parámetro.
     *
     * <p>{@code HttpMessageNotReadableException} cubre cosas como enviar un string donde
     * se espera un número o un valor de enum que no existe. Se agrupa con los otros 400
     * porque el origen es siempre un error del cliente, no del servidor.</p>
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        ApiError error = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Token ausente, inválido o caducado; lo lanza el {@code AuthInterceptor}.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        ApiError error = buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Captura cualquier excepción no contemplada en los handlers anteriores.
     *
     * <p>El mensaje del error interno se oculta al cliente de forma deliberada
     * para no exponer detalles de implementación ni mensajes de base de datos.
     * El stacktrace completo sí queda en los logs del servidor.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        ApiError error = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno en el servidor",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ApiError buildError(HttpStatus status, String message, String path) {
        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}