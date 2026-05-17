package com.nutrifit.backend.tendencias.controller;

import com.nutrifit.backend.common.exception.UnauthorizedException;
import com.nutrifit.backend.tendencias.dto.TendenciasResponse;
import com.nutrifit.backend.tendencias.service.TendenciasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tendencias", description = "Evolución nutricional histórica del usuario (hasta 90 días)")
@RestController
@RequestMapping("/api/tendencias")
public class TendenciasController {

    private final TendenciasService service;

    public TendenciasController(TendenciasService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener tendencias nutricionales del usuario")
    @ApiResponse(responseCode = "200", description = "Tendencias calculadas")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @GetMapping
    public TendenciasResponse getTendencias(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "30") int dias,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.getTendencias(usuarioId, Math.min(dias, 90));
    }
}
