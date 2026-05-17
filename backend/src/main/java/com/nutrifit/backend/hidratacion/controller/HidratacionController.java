package com.nutrifit.backend.hidratacion.controller;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import com.nutrifit.backend.hidratacion.dto.HidratacionDiariaResponse;
import com.nutrifit.backend.hidratacion.service.HidratacionService;
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

@Tag(name = "Hidratación", description = "Registro y consulta de ingesta de agua")
@RestController
@RequestMapping("/api/hidratacion")
public class HidratacionController {

    private final HidratacionService service;

    public HidratacionController(HidratacionService service) {
        this.service = service;
    }

    @Operation(summary = "Registrar ingesta de agua")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Registro creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AguaResponse registrar(
            @RequestParam Long usuarioId,
            @Valid @RequestBody AguaRequest request,
            HttpServletRequest httpRequest) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.registrar(usuarioId, request);
    }

    @Operation(summary = "Obtener resumen de hidratación diaria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumen obtenido"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public HidratacionDiariaResponse getDiario(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.getDiario(usuarioId, fecha);
    }

    @Operation(summary = "Eliminar registro de hidratación")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Registro eliminado"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Registro no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        service.eliminar(usuarioId, id);
    }
}
