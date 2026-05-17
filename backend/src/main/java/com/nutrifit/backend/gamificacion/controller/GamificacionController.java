package com.nutrifit.backend.gamificacion.controller;

import com.nutrifit.backend.gamificacion.dto.GamificacionResponse;
import com.nutrifit.backend.gamificacion.service.GamificacionService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Gamificación", description = "Estadísticas de gamificación del usuario")
@RestController
@RequestMapping("/api/gamificacion")
public class GamificacionController {

    private final GamificacionService gamificacionService;

    public GamificacionController(GamificacionService gamificacionService) {
        this.gamificacionService = gamificacionService;
    }

    @Operation(summary = "Obtener estadísticas de gamificación")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas calculadas"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public GamificacionResponse getGamificacion(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return gamificacionService.calcular(usuarioId, fecha);
    }
}
