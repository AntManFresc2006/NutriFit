package com.nutrifit.backend.ejercicio.controller;

import com.nutrifit.backend.ejercicio.dto.EjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.EjercicioResponse;
import com.nutrifit.backend.ejercicio.service.EjercicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @GetMapping
    public List<EjercicioResponse> getAll(@RequestParam(required = false) String q) {
        return ejercicioService.findAll(q);
    }

    @GetMapping("/{id}")
    public EjercicioResponse getById(@PathVariable Long id) {
        return ejercicioService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EjercicioResponse create(@Valid @RequestBody EjercicioRequest request) {
        return ejercicioService.save(request);
    }
}
