package com.nutrifit.backend.perfil.controller;

import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para consultar y actualizar el perfil del usuario.
 */
@Tag(name = "Perfil", description = "Gestión del perfil e información biométrica del usuario")
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
    @Operation(summary = "Obtener perfil del usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil obtenido"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
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
    @Operation(summary = "Actualizar perfil del usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
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
