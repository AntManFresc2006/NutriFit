package com.nutrifit.backend.ejercicio.controller;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.service.RegistroEjercicioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ejercicios-registro")
public class RegistroEjercicioController {

    private final RegistroEjercicioService registroService;

    public RegistroEjercicioController(RegistroEjercicioService registroService) {
        this.registroService = registroService;
    }

    @GetMapping
    public List<RegistroEjercicioResponse> getByFecha(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return registroService.findByUsuarioAndFecha(usuarioId, fecha);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroEjercicioResponse registrar(
            @RequestParam Long usuarioId,
            @Valid @RequestBody RegistroEjercicioRequest request) {
        return registroService.registrar(usuarioId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long usuarioId,
            @PathVariable Long id) {
        registroService.deleteById(usuarioId, id);
    }
}
