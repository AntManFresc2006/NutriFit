package com.nutrifit.backend.perfil.controller;

import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para consultar y actualizar el perfil del usuario.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * Devuelve el perfil completo del usuario junto con TMB y TDEE calculados.
     */
    @GetMapping("/{id}")
    public PerfilResponse getPerfil(@PathVariable Long id, HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!id.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return perfilService.getPerfil(id);
    }

    /**
     * Actualiza los datos biométricos y el objetivo del usuario.
     * Devuelve el perfil actualizado con TMB y TDEE recalculados.
     */
    @PutMapping("/{id}")
    public PerfilResponse updatePerfil(
            @PathVariable Long id,
            @Valid @RequestBody PerfilUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!id.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return perfilService.updatePerfil(id, request);
    }
}
