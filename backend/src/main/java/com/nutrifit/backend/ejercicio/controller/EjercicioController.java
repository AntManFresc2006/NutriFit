package com.nutrifit.backend.ejercicio.controller;

import com.nutrifit.backend.ejercicio.dto.EjercicioExternoResponse;
import com.nutrifit.backend.ejercicio.dto.EjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.EjercicioResponse;
import com.nutrifit.backend.ejercicio.service.EjercicioService;
import com.nutrifit.backend.ejercicio.service.WgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints para el catálogo de ejercicios disponibles en NutriFit.
 *
 * <p>Los ejercicios son entradas del catálogo compartido (no pertenecen a un usuario
 * concreto). Los registros de actividad diaria del usuario se gestionan en
 * {@code RegistroEjercicioController}.</p>
 */
@Tag(name = "Ejercicios", description = "Catálogo de ejercicios disponibles")
@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioController {

    private final EjercicioService ejercicioService;
    private final WgerService wgerService;

    public EjercicioController(EjercicioService ejercicioService, WgerService wgerService) {
        this.ejercicioService = ejercicioService;
        this.wgerService = wgerService;
    }

    /**
     * Devuelve el catálogo completo de ejercicios, con filtro opcional por nombre.
     *
     * @param q texto opcional para buscar ejercicios por nombre (case-insensitive)
     * @return lista de ejercicios con su MET y categoría
     */
    @Operation(summary = "Obtener catálogo de ejercicios")
    @ApiResponse(responseCode = "200", description = "Lista de ejercicios")
    @GetMapping
    public List<EjercicioResponse> getAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tipo) {
        return ejercicioService.findAll(q, tipo);
    }

    /**
     * Obtiene un ejercicio concreto del catálogo por su id.
     *
     * @param id identificador del ejercicio
     * @return datos del ejercicio incluido su valor MET
     */
    @Operation(summary = "Obtener ejercicio por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ejercicio encontrado"),
        @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @GetMapping("/{id}")
    public EjercicioResponse getById(@PathVariable Long id) {
        return ejercicioService.findById(id);
    }

    /**
     * Añade un nuevo ejercicio al catálogo compartido.
     *
     * @param request nombre, MET (0.1–20.0) y categoría del ejercicio
     * @return ejercicio creado con su id asignado
     */
    @Operation(summary = "Crear nuevo ejercicio")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ejercicio creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EjercicioResponse create(@Valid @RequestBody EjercicioRequest request) {
        return ejercicioService.save(request);
    }

    @Operation(summary = "Buscar ejercicios en catálogo externo")
    @ApiResponse(responseCode = "200", description = "Resultados de búsqueda")
    @GetMapping("/externo")
    public List<EjercicioExternoResponse> buscarExterno(@RequestParam String q) {
        if (q == null || q.isBlank()) return List.of();
        return wgerService.buscar(q);
    }
}
