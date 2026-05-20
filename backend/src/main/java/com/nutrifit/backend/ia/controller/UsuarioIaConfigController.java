package com.nutrifit.backend.ia.controller;

import com.nutrifit.backend.common.exception.UnauthorizedException;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigRequest;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;
import com.nutrifit.backend.ia.service.UsuarioIaConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar la configuración de IA personalizada del usuario.
 */
@Tag(name = "Opciones de IA", description = "Gestión de configuración personalizada de IA")
@RestController
@RequestMapping("/api/ia-config")
public class UsuarioIaConfigController {

    private static final String AUTH_USER_ATTR = "authenticatedUserId";

    private final UsuarioIaConfigService service;

    public UsuarioIaConfigController(UsuarioIaConfigService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener configuración de IA del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuración obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "No existe configuración para este usuario")
    })
    @GetMapping
    public ResponseEntity<UsuarioIaConfigResponse> getConfig(
            @Parameter(description = "ID del usuario")
            @RequestParam Long usuarioId,
            HttpServletRequest httpRequest) {
        Long authId = (Long) httpRequest.getAttribute(AUTH_USER_ATTR);
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.getConfig(usuarioId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear o actualizar configuración de IA del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuración guardada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping
    public ResponseEntity<UsuarioIaConfigResponse> saveConfig(
            @Parameter(description = "ID del usuario")
            @RequestParam Long usuarioId,
            @Valid @RequestBody UsuarioIaConfigRequest request,
            HttpServletRequest httpRequest) {
        Long authId = (Long) httpRequest.getAttribute(AUTH_USER_ATTR);
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        UsuarioIaConfigResponse response = service.saveConfig(usuarioId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar configuración de IA del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configuración eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteConfig(
            @Parameter(description = "ID del usuario")
            @RequestParam Long usuarioId,
            HttpServletRequest httpRequest) {
        Long authId = (Long) httpRequest.getAttribute(AUTH_USER_ATTR);
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        service.deleteConfig(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
