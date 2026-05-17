package com.nutrifit.backend.plansemanal.controller;

import com.nutrifit.backend.common.exception.UnauthorizedException;
import com.nutrifit.backend.plansemanal.dto.PlanSemanalRequest;
import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;
import com.nutrifit.backend.plansemanal.service.PlanSemanalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/plan-semanal")
public class PlanSemanalController {

    private final PlanSemanalService planSemanalService;

    public PlanSemanalController(PlanSemanalService planSemanalService) {
        this.planSemanalService = planSemanalService;
    }

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
