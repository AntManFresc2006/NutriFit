package com.nutrifit.backend.resumen.controller;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.service.ResumenDiarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Controlador REST del resumen nutricional diario.
 */
@RestController
@RequestMapping("/api/resumen-diario")
public class ResumenDiarioController {

    private final ResumenDiarioService resumenDiarioService;

    public ResumenDiarioController(ResumenDiarioService resumenDiarioService) {
        this.resumenDiarioService = resumenDiarioService;
    }

    @GetMapping
    public ResumenDiarioResponse getResumenDiario(
            @RequestParam Long usuarioId,
            @RequestParam LocalDate fecha
    ) {
        return resumenDiarioService.obtenerResumenDiario(usuarioId, fecha);
    }
}
