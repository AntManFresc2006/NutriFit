package com.nutrifit.backend.resumen.controller;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.service.ResumenDiarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Endpoint que agrega la ingesta nutricional y el gasto calórico de un día.
 *
 * <p>Devuelve el balance neto (kcal comidas − kcal ejercicios) que el cliente
 * muestra junto al TDEE del perfil para contextualizar el resultado.</p>
 */
@RestController
@RequestMapping("/api/resumen-diario")
public class ResumenDiarioController {

    private final ResumenDiarioService resumenDiarioService;

    public ResumenDiarioController(ResumenDiarioService resumenDiarioService) {
        this.resumenDiarioService = resumenDiarioService;
    }

    /**
     * Calcula los totales nutricionales y el balance calórico neto del día.
     *
     * <p>Si el usuario no tiene comidas registradas para esa fecha, devuelve ceros
     * en lugar de un 404, porque "ningún dato" es un estado válido del día.</p>
     *
     * @param usuarioId id del usuario autenticado
     * @param fecha     día a resumir en formato ISO-8601 (yyyy-MM-dd)
     * @return totales de kcal, macros, kcal quemadas y balance neto
     */
    @GetMapping
    public ResumenDiarioResponse getResumenDiario(
            @RequestParam Long usuarioId,
            @RequestParam LocalDate fecha
    ) {
        return resumenDiarioService.obtenerResumenDiario(usuarioId, fecha);
    }
}
