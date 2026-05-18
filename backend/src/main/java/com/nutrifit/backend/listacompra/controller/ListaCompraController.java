package com.nutrifit.backend.listacompra.controller;

import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.SugerenciasResponse;
import com.nutrifit.backend.listacompra.service.ListaCompraService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * Controlador REST para gestionar la lista de compra del usuario.
 */
@Tag(name = "Lista de la Compra", description = "Gestión de lista de compra personal")
@RestController
@RequestMapping("/api/lista-compra")
public class ListaCompraController {

    private final ListaCompraService service;

    public ListaCompraController(ListaCompraService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener artículos de la lista de compra")
    @ApiResponse(responseCode = "200", description = "Lista de artículos obtenida")
    @GetMapping
    public Map<String, List<ListaCompraItemResponse>> getItems(
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.getItems(usuarioId);
    }

    @Operation(summary = "Añadir artículo a la lista")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Artículo añadido"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListaCompraItemResponse addItem(
            @RequestParam Long usuarioId,
            @Valid @RequestBody ListaCompraItemRequest req,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.addItem(usuarioId, req);
    }

    @Operation(summary = "Cambiar estado de completado del artículo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado")
    })
    @PatchMapping("/{id}/toggle")
    public ListaCompraItemResponse toggleCompletado(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.toggle(usuarioId, id);
    }

    @Operation(summary = "Eliminar artículo de la lista")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Artículo eliminado"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        service.delete(usuarioId, id);
    }

    @Operation(summary = "Limpiar artículos completados")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Artículos completados eliminados"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/completados")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCompletados(
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        service.clearCompletados(usuarioId);
    }

    @Operation(summary = "Obtener sugerencias de alimentos")
    @ApiResponse(responseCode = "200", description = "Sugerencias generadas")
    @GetMapping("/sugerencias")
    public SugerenciasResponse getSugerencias(
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.getSugerencias(usuarioId);
    }
}
