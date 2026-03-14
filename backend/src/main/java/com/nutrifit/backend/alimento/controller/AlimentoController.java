package com.nutrifit.backend.alimento.controller;

import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.service.AlimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alimentos")
public class AlimentoController {

    private final AlimentoService alimentoService;

    public AlimentoController(AlimentoService alimentoService) {
        this.alimentoService = alimentoService;
    }

    @GetMapping
    public List<AlimentoResponse> getAll(@RequestParam(required = false) String q) {
        return alimentoService.findAll(q);
    }

    @GetMapping("/{id}")
    public AlimentoResponse getById(@PathVariable Long id) {
        return alimentoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlimentoResponse create(@Valid @RequestBody AlimentoRequest request) {
        return alimentoService.save(request);
    }

    @PutMapping("/{id}")
    public AlimentoResponse update(@PathVariable Long id, @Valid @RequestBody AlimentoRequest request) {
        return alimentoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        alimentoService.deleteById(id);
    }
}