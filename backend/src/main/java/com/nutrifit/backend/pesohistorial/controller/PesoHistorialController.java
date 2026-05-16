package com.nutrifit.backend.pesohistorial.controller;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialRequest;
import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import com.nutrifit.backend.pesohistorial.service.PesoHistorialService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/peso-historial")
public class PesoHistorialController {

    private final PesoHistorialService service;

    public PesoHistorialController(PesoHistorialService service) {
        this.service = service;
    }

    @GetMapping
    public List<PesoHistorialResponse> getHistorial(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "30") int limit) {
        return service.findByUsuario(usuarioId, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PesoHistorialResponse registrar(
            @RequestParam Long usuarioId,
            @Valid @RequestBody PesoHistorialRequest request) {
        if (!request.isValidPeso()) {
            throw new IllegalArgumentException("El peso debe estar entre 20 y 500 kg");
        }
        LocalDate fecha = LocalDate.parse(request.getFecha());
        return service.upsert(usuarioId, fecha, request.getPesoKg());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        service.deleteByUsuarioAndFecha(usuarioId, fecha);
    }
}
