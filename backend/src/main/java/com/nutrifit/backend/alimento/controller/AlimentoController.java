package com.nutrifit.backend.alimento.controller;

import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.service.AlimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del módulo de alimentos.
 * Expone los endpoints del CRUD de alimentos bajo la ruta base /api/alimentos
 * y delega la lógica de negocio en la capa de servicio.
 */
@RestController
@RequestMapping("/api/alimentos")
public class AlimentoController {

    private final AlimentoService alimentoService;

    /**
     * Inyección de dependencias mediante constructor.
     * Recibe el servicio encargado de gestionar la lógica del módulo de alimentos.
     */
    public AlimentoController(AlimentoService alimentoService) {
        this.alimentoService = alimentoService;
    }

    /**
     * Obtiene todos los alimentos o filtra por nombre si se proporciona un texto de búsqueda.
     *
     * @param q texto opcional para filtrar alimentos por nombre
     * @return lista de alimentos en formato DTO de respuesta
     */
    @GetMapping
    public List<AlimentoResponse> getAll(@RequestParam(required = false) String q) {
        return alimentoService.findAll(q);
    }

    /**
     * Obtiene un alimento concreto a partir de su identificador.
     *
     * @param id identificador único del alimento
     * @return alimento encontrado en formato DTO de respuesta
     */
    @GetMapping("/{id}")
    public AlimentoResponse getById(@PathVariable Long id) {
        return alimentoService.findById(id);
    }

    /**
     * Crea un nuevo alimento.
     *
     * @param request datos del alimento a crear, validados automáticamente
     * @return alimento creado en formato DTO de respuesta
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlimentoResponse create(@Valid @RequestBody AlimentoRequest request) {
        return alimentoService.save(request);
    }

    /**
     * Actualiza un alimento existente a partir de su identificador.
     *
     * @param id identificador del alimento a actualizar
     * @param request nuevos datos del alimento, validados automáticamente
     * @return alimento actualizado en formato DTO de respuesta
     */
    @PutMapping("/{id}")
    public AlimentoResponse update(@PathVariable Long id, @Valid @RequestBody AlimentoRequest request) {
        return alimentoService.update(id, request);
    }

    /**
     * Elimina un alimento existente por su identificador.
     *
     * @param id identificador del alimento a eliminar
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        alimentoService.deleteById(id);
    }
}