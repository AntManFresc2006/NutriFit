package com.nutrifit.backend.listacompra.controller;

import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.SugerenciasResponse;
import com.nutrifit.backend.listacompra.service.ListaCompraService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/lista-compra")
public class ListaCompraController {

    private final ListaCompraService service;

    public ListaCompraController(ListaCompraService service) {
        this.service = service;
    }

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
