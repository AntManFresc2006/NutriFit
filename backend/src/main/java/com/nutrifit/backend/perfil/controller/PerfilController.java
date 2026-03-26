package com.nutrifit.backend.perfil.controller;

import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;
import com.nutrifit.backend.perfil.service.PerfilService;
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
    public PerfilResponse getPerfil(@PathVariable Long id) {
        return perfilService.getPerfil(id);
    }

    /**
     * Actualiza los datos biométricos y el objetivo del usuario.
     * Devuelve el perfil actualizado con TMB y TDEE recalculados.
     */
    @PutMapping("/{id}")
    public PerfilResponse updatePerfil(
            @PathVariable Long id,
            @Valid @RequestBody PerfilUpdateRequest request
    ) {
        return perfilService.updatePerfil(id, request);
    }
}
