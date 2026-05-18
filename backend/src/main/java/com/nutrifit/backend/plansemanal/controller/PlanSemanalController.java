package com.nutrifit.backend.plansemanal.controller;

import com.nutrifit.backend.common.exception.UnauthorizedException;
import com.nutrifit.backend.plansemanal.dto.PlanSemanalRequest;
import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;
import com.nutrifit.backend.plansemanal.service.PlanSemanalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador REST para generar y gestionar planes semanales de alimentación.
 */
@Tag(name = "Plan Semanal", description = "Generación y gestión de planes alimentarios semanales")
@RestController
@RequestMapping("/api/plan-semanal")
public class PlanSemanalController {

    private final PlanSemanalService planSemanalService;

    public PlanSemanalController(PlanSemanalService planSemanalService) {
        this.planSemanalService = planSemanalService;
    }

    @Operation(summary = "Generar o recuperar plan semanal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Plan generado o recuperado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "500", description = "Error al generar el plan")
    })
    @PostMapping
    public ResponseEntity<PlanSemanalResponse> generarPlan(
            @RequestParam Long usuarioId,
            @Valid @RequestBody PlanSemanalRequest request,
            HttpServletRequest httpRequest
    ) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }

        try {
            PlanSemanalResponse response = planSemanalService.generarORecuperarPlan(
                    usuarioId,
                    request.getSemanaInicio()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Obtener plan semanal existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Plan obtenido"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Plan no encontrado")
    })
    @GetMapping
    public ResponseEntity<PlanSemanalResponse> obtenerPlan(
            @RequestParam Long usuarioId,
            @RequestParam LocalDate semanaInicio,
            HttpServletRequest httpRequest
    ) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }

        PlanSemanalResponse response = planSemanalService.getPlan(usuarioId, semanaInicio);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar plan semanal")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Plan eliminado"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Plan no encontrado")
    })
    @DeleteMapping
    public ResponseEntity<Void> eliminarPlan(
            @RequestParam Long usuarioId,
            @RequestParam LocalDate semanaInicio,
            HttpServletRequest httpRequest
    ) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }

        planSemanalService.eliminarPlan(usuarioId, semanaInicio);
        return ResponseEntity.noContent().build();
    }
}
