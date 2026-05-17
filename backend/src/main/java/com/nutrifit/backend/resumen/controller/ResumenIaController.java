package com.nutrifit.backend.resumen.controller;

import com.nutrifit.backend.resumen.dto.EvaluacionIaRequest;
import com.nutrifit.backend.resumen.service.EvaluacionIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Evaluación IA", description = "Evaluación nutricional personalizada mediante inteligencia artificial")
@RestController
@RequestMapping("/api/resumen")
public class ResumenIaController {

    private final EvaluacionIaService evaluacionIaService;
    private final JdbcTemplate jdbcTemplate;

    public ResumenIaController(EvaluacionIaService evaluacionIaService, JdbcTemplate jdbcTemplate) {
        this.evaluacionIaService = evaluacionIaService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Operation(summary = "Generar evaluación nutricional con IA", description = "Analiza los últimos 7 días de ingesta y ejercicio y devuelve una evaluación personalizada")
    @ApiResponse(responseCode = "200", description = "Evaluación generada correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "500", description = "Error al contactar el modelo IA")
    @PostMapping("/evaluacion-ia")
    public ResponseEntity<Map<String, String>> evaluar(@Valid @RequestBody EvaluacionIaRequest request) {
        try {
            LocalDate fechaFin = LocalDate.parse(request.getFecha());
            Context7Dias context = calcular7DiasContext(request.getUsuarioId(), fechaFin, request.getTdee());
            request.setKcalMedia7d(context.kcalMedia7d);
            request.setProteinasMedia7d(context.proteinasMedia7d);
            request.setDiasConEjercicio7d(context.diasConEjercicio7d);
            request.setBalanceMedia7d(context.balanceMedia7d);

            String evaluacion = evaluacionIaService.evaluar(request);
            return ResponseEntity.ok(Map.of("evaluacion", evaluacion));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al generar evaluación: " + e.getMessage()));
        }
    }

    private Context7Dias calcular7DiasContext(Long usuarioId, LocalDate fechaFin, double tdee) {
        LocalDate fechaInicio = fechaFin.minusDays(7);

        // Query 1: Ingesta últimos 7 días
        String sqlIngesta = """
                SELECT
                    c.fecha,
                    COALESCE(SUM((a.kcal_por_100g * ca.gramos) / 100.0), 0) AS kcal_dia,
                    COALESCE(SUM((a.proteinas_g   * ca.gramos) / 100.0), 0) AS proteinas_dia
                FROM comidas c
                LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                LEFT JOIN alimentos a ON a.id = ca.alimento_id
                WHERE c.usuario_id = ? AND c.fecha > ? AND c.fecha <= ?
                GROUP BY c.fecha
                """;

        Map<String, Double[]> diasConDatos = new java.util.LinkedHashMap<>();
        jdbcTemplate.query(sqlIngesta, rs -> {
            String fecha = rs.getDate("fecha").toString();
            double kcal = rs.getDouble("kcal_dia");
            double proteinas = rs.getDouble("proteinas_dia");
            diasConDatos.put(fecha, new Double[]{kcal, proteinas});
        }, usuarioId, fechaInicio, fechaFin);

        double kcalMedia7d = diasConDatos.values().stream()
                .mapToDouble(arr -> arr[0])
                .average()
                .orElse(0);

        double proteinasMedia7d = diasConDatos.values().stream()
                .mapToDouble(arr -> arr[1])
                .average()
                .orElse(0);

        // Query 2: Ejercicio últimos 7 días
        String sqlEjercicio = """
                SELECT
                    COUNT(DISTINCT fecha) AS dias_con_ejercicio,
                    COALESCE(SUM(kcal_quemadas) / 7.0, 0) AS kcal_quemadas_media
                FROM ejercicios_registro
                WHERE usuario_id = ? AND fecha > ? AND fecha <= ?
                """;

        Integer diasConEjercicio7d = 0;
        double kcalQuemadasMedia = 0;

        Map<String, Object> resultEjercicio = jdbcTemplate.queryForMap(sqlEjercicio, usuarioId, fechaInicio, fechaFin);
        if (resultEjercicio != null) {
            Object diasObj = resultEjercicio.get("dias_con_ejercicio");
            diasConEjercicio7d = (diasObj instanceof Number) ? ((Number) diasObj).intValue() : 0;
            Object kcalObj = resultEjercicio.get("kcal_quemadas_media");
            kcalQuemadasMedia = (kcalObj instanceof Number) ? ((Number) kcalObj).doubleValue() : 0;
        }

        double balanceMedia7d = kcalMedia7d - tdee - kcalQuemadasMedia;

        return new Context7Dias(kcalMedia7d, proteinasMedia7d, diasConEjercicio7d, balanceMedia7d);
    }

    private record Context7Dias(
        double kcalMedia7d,
        double proteinasMedia7d,
        int diasConEjercicio7d,
        double balanceMedia7d
    ) {}
}
