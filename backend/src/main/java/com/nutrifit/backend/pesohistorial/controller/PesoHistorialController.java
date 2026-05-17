package com.nutrifit.backend.pesohistorial.controller;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialRequest;
import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import com.nutrifit.backend.pesohistorial.service.PesoHistorialService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Historial de Peso", description = "Registro y consulta del historial de peso del usuario")
@RestController
@RequestMapping("/api/peso-historial")
public class PesoHistorialController {

    private final PesoHistorialService service;

    public PesoHistorialController(PesoHistorialService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener historial de peso del usuario")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    @GetMapping
    public List<PesoHistorialResponse> getHistorial(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "30") int limit,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.findByUsuario(usuarioId, limit);
    }

    @Operation(summary = "Registrar o actualizar peso")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Registro creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PesoHistorialResponse registrar(
            @RequestParam Long usuarioId,
            @Valid @RequestBody PesoHistorialRequest request,
            HttpServletRequest httpRequest) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        if (!request.isValidPeso()) {
            throw new IllegalArgumentException("El peso debe estar entre 20 y 500 kg");
        }
        LocalDate fecha = LocalDate.parse(request.getFecha());
        return service.upsert(usuarioId, fecha, request.getPesoKg());
    }

    @Operation(summary = "Eliminar registro de peso")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Registro eliminado"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Registro no encontrado")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        service.deleteByUsuarioAndFecha(usuarioId, fecha);
    }
}
