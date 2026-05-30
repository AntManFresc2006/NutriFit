package com.nutrifit.backend.detective.controller;

import com.nutrifit.backend.auth.security.IaRateLimiter;
import com.nutrifit.backend.common.exception.TooManyRequestsException;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import com.nutrifit.backend.detective.dto.DetectiveAnalisisDto;
import com.nutrifit.backend.detective.service.DetectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Detective Nutricional", description = "Análisis forense del historial nutricional del usuario")
@RestController
@RequestMapping("/api/detective")
public class DetectiveController {

    private final DetectiveService detectiveService;
    private final IaRateLimiter iaRateLimiter;

    public DetectiveController(DetectiveService detectiveService, IaRateLimiter iaRateLimiter) {
        this.detectiveService = detectiveService;
        this.iaRateLimiter = iaRateLimiter;
    }

    @Operation(summary = "Iniciar análisis forense nutricional")
    @PostMapping
    public ResponseEntity<DetectiveAnalisisDto> iniciarAnalisis(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "30") int dias,
            HttpServletRequest httpRequest
    ) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        if (!iaRateLimiter.permitir(usuarioId)) {
            throw new TooManyRequestsException("Límite de análisis alcanzado. Espera un minuto.");
        }
        int diasLimitado = Math.max(7, Math.min(dias, 90));
        DetectiveAnalisisDto result = detectiveService.iniciarAnalisis(usuarioId, diasLimitado);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener análisis en curso o completado")
    @GetMapping
    public ResponseEntity<DetectiveAnalisisDto> getAnalisis(
            @RequestParam Long usuarioId,
            HttpServletRequest httpRequest
    ) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        DetectiveAnalisisDto result = detectiveService.getAnalisis(usuarioId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
