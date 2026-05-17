package com.nutrifit.backend.reto.controller;

import com.nutrifit.backend.reto.dto.AceptarRetoRequest;
import com.nutrifit.backend.reto.dto.RetoResponse;
import com.nutrifit.backend.reto.service.RetoService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/retos")
public class RetoController {

    private final RetoService retoService;

    public RetoController(RetoService retoService) {
        this.retoService = retoService;
    }

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
