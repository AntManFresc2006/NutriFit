package com.nutrifit.backend.alimento.controller;

import com.nutrifit.backend.alimento.dto.AlimentoExternoResponse;
import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.dto.EscanearFotoRequest;
import com.nutrifit.backend.alimento.dto.EscanearFotoResponse;
import com.nutrifit.backend.alimento.service.AlimentoService;
import com.nutrifit.backend.alimento.service.OpenFoodFactsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del módulo de alimentos.
 * Expone los endpoints del CRUD de alimentos bajo la ruta base /api/alimentos
 * y delega la lógica de negocio en la capa de servicio.
 */
@Tag(name = "Alimentos", description = "Gestión del catálogo de alimentos")
@RestController
@RequestMapping("/api/alimentos")
public class AlimentoController {

    private final AlimentoService alimentoService;
    private final OpenFoodFactsService openFoodFactsService;

    public AlimentoController(AlimentoService alimentoService, OpenFoodFactsService openFoodFactsService) {
        this.alimentoService = alimentoService;
        this.openFoodFactsService = openFoodFactsService;
    }

    /**
     * Obtiene todos los alimentos o filtra por nombre si se proporciona un texto de búsqueda.
     *
     * @param q texto opcional para filtrar alimentos por nombre
     * @return lista de alimentos en formato DTO de respuesta
     */
    @Operation(summary = "Listar alimentos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alimentos recuperados exitosamente")
    })
    @GetMapping
    public List<AlimentoResponse> getAll(
            @Parameter(description = "Texto para filtrar alimentos por nombre (opcional)")
            @RequestParam(required = false) String q) {
        return alimentoService.findAll(q);
    }

    /**
     * Obtiene un alimento concreto a partir de su identificador.
     *
     * @param id identificador único del alimento
     * @return alimento encontrado en formato DTO de respuesta
     */
    @Operation(summary = "Obtener alimento por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alimento recuperado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Alimento no encontrado")
    })
    @GetMapping("/{id}")
    public AlimentoResponse getById(
            @Parameter(description = "ID del alimento")
            @PathVariable Long id) {
        return alimentoService.findById(id);
    }

    /**
     * Crea un nuevo alimento.
     *
     * @param request datos del alimento a crear, validados automáticamente
     * @return alimento creado en formato DTO de respuesta
     */
    @Operation(summary = "Crear nuevo alimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Alimento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
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
    @Operation(summary = "Actualizar alimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alimento actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Alimento no encontrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PutMapping("/{id}")
    public AlimentoResponse update(
            @Parameter(description = "ID del alimento")
            @PathVariable Long id,
            @Valid @RequestBody AlimentoRequest request) {
        return alimentoService.update(id, request);
    }

    /**
     * Elimina un alimento existente por su identificador.
     *
     * @param id identificador del alimento a eliminar
     */
    @Operation(summary = "Eliminar alimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Alimento eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Alimento no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "ID del alimento")
            @PathVariable Long id) {
        alimentoService.deleteById(id);
    }

    @Operation(summary = "Buscar alimentos externos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetro de búsqueda requerido")
    })
    @GetMapping("/externo")
    public List<AlimentoExternoResponse> buscarExterno(
            @Parameter(description = "Texto de búsqueda de alimentos")
            @RequestParam String q) {
        if (q == null || q.isBlank()) return List.of();
        return openFoodFactsService.buscar(q);
    }

    @Operation(summary = "Escanear producto con IA desde foto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Análisis completado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "500", description = "Error al procesar la imagen")
    })
    @PostMapping("/escanear-foto")
    public EscanearFotoResponse escanearFoto(@Valid @RequestBody EscanearFotoRequest request) {
        try {
            return alimentoService.escanearFoto(request.getImagenBase64(), request.getMimeType());
        } catch (Exception e) {
            throw new RuntimeException("Error al escanear foto: " + e.getMessage(), e);
        }
    }
}