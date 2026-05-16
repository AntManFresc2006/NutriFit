package com.nutrifit.backend.resumen.controller;

import com.nutrifit.backend.resumen.dto.EvaluacionIaRequest;
import com.nutrifit.backend.resumen.service.EvaluacionIaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/resumen")
public class ResumenIaController {

    private final EvaluacionIaService evaluacionIaService;

    public ResumenIaController(EvaluacionIaService evaluacionIaService) {
        this.evaluacionIaService = evaluacionIaService;
    }

    @PostMapping("/evaluacion-ia")
    public ResponseEntity<Map<String, String>> evaluar(@RequestBody EvaluacionIaRequest request) {
        try {
            String evaluacion = evaluacionIaService.evaluar(request);
            return ResponseEntity.ok(Map.of("evaluacion", evaluacion));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al generar evaluación: " + e.getMessage()));
        }
    }
}
