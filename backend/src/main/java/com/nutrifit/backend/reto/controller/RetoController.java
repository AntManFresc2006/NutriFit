package com.nutrifit.backend.reto.controller;

import com.nutrifit.backend.reto.dto.AceptarRetoRequest;
import com.nutrifit.backend.reto.dto.RetoResponse;
import com.nutrifit.backend.reto.service.RetoService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar retos y su progreso.
 */
@Tag(name = "Retos", description = "Desafíos de fitness y seguimiento de progreso")
@RestController
@RequestMapping("/api/retos")
public class RetoController {

    private final RetoService retoService;

    public RetoController(RetoService retoService) {
        this.retoService = retoService;
    }

    @Operation(summary = "Listar retos del usuario")
    @ApiResponse(responseCode = "200", description = "Lista de retos devuelta")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @GetMapping
    public List<RetoResponse> getRetos(
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return retoService.getRetos(usuarioId);
    }

    @Operation(summary = "Aceptar un reto")
    @ApiResponse(responseCode = "200", description = "Reto aceptado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @PostMapping("/aceptar")
    public RetoResponse aceptarReto(
            @RequestParam Long usuarioId,
            @Valid @RequestBody AceptarRetoRequest req,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return retoService.aceptarReto(usuarioId, req);
    }

    @Operation(summary = "Sincronizar progreso de retos con actividad del día")
    @ApiResponse(responseCode = "200", description = "Progreso actualizado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @PostMapping("/sincronizar")
    public List<RetoResponse> sincronizarProgreso(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return retoService.sincronizarProgreso(usuarioId, fecha);
    }

    @Operation(summary = "Abandonar un reto aceptado")
    @ApiResponse(responseCode = "200", description = "Reto abandonado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @DeleteMapping("/{usuarioRetoId}")
    public void abandonarReto(
            @RequestParam Long usuarioId,
            @PathVariable Long usuarioRetoId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        retoService.abandonarReto(usuarioId, usuarioRetoId);
    }
}
