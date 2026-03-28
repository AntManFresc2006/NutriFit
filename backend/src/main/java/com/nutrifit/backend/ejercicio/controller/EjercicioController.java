package com.nutrifit.backend.ejercicio.controller;

import com.nutrifit.backend.ejercicio.dto.EjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.EjercicioResponse;
import com.nutrifit.backend.ejercicio.service.EjercicioService;
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
@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    /**
     * Devuelve el catálogo completo de ejercicios, con filtro opcional por nombre.
     *
     * @param q texto opcional para buscar ejercicios por nombre (case-insensitive)
     * @return lista de ejercicios con su MET y categoría
     */
    @GetMapping
    public List<EjercicioResponse> getAll(@RequestParam(required = false) String q) {
        return ejercicioService.findAll(q);
    }

    /**
     * Obtiene un ejercicio concreto del catálogo por su id.
     *
     * @param id identificador del ejercicio
     * @return datos del ejercicio incluido su valor MET
     */
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EjercicioResponse create(@Valid @RequestBody EjercicioRequest request) {
        return ejercicioService.save(request);
    }
}
