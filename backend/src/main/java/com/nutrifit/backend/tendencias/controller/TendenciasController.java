package com.nutrifit.backend.tendencias.controller;

import com.nutrifit.backend.common.exception.UnauthorizedException;
import com.nutrifit.backend.tendencias.dto.TendenciasResponse;
import com.nutrifit.backend.tendencias.service.TendenciasService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tendencias")
public class TendenciasController {

    private final TendenciasService service;

    public TendenciasController(TendenciasService service) {
        this.service = service;
    }

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
