package com.nutrifit.backend.comida.controller;

import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
import com.nutrifit.backend.comida.service.ComidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.nutrifit.backend.comida.dto.ComidaAlimentoRequest;
import com.nutrifit.backend.comida.model.ComidaAlimento;
import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST del módulo de comidas.
 */
@RestController
@RequestMapping("/api/comidas")
public class ComidaController {

    private final ComidaService comidaService;

    public ComidaController(ComidaService comidaService) {
        this.comidaService = comidaService;
    }

    /**
     * Lista comidas de un usuario para una fecha concreta.
     */
    @GetMapping
    public List<ComidaResponse> getByUsuarioAndFecha(
            @RequestParam Long usuarioId,
            @RequestParam LocalDate fecha
    ) {
        return comidaService.findByUsuarioAndFecha(usuarioId, fecha);
    }

    /**
     * Crea una nueva comida para un usuario.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComidaResponse create(
            @RequestParam Long usuarioId,
            @Valid @RequestBody ComidaRequest request
    ) {
        return comidaService.save(usuarioId, request);
    }

    /**
 * Añade un alimento a una comida existente.
 */
@PostMapping("/{comidaId}/items")
@ResponseStatus(HttpStatus.CREATED)
public void addAlimento(
        @PathVariable Long comidaId,
        @Valid @RequestBody ComidaAlimentoRequest request
) {
    comidaService.addAlimentoToComida(comidaId, request);
}

    /**
     * Elimina una comida y todos sus items.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        comidaService.deleteById(id);
    }

    /**
     * Lista los alimentos añadidos a una comida.
     */
    @GetMapping("/{comidaId}/items")
    public List<ComidaItemDetalleResponse> getItems(@PathVariable Long comidaId) {
        return comidaService.findDetalleItemsByComidaId(comidaId);
    }

    /**
     * Elimina un item concreto de una comida.
     */
    @DeleteMapping("/{comidaId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long comidaId, @PathVariable Long itemId) {
        comidaService.deleteItem(comidaId, itemId);
    }
}