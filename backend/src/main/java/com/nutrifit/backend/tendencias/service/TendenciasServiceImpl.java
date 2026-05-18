package com.nutrifit.backend.tendencias.service;

import com.nutrifit.backend.gamificacion.service.GamificacionService;
import com.nutrifit.backend.gamificacion.dto.GamificacionResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.tendencias.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementación del servicio de tendencias nutricionales.
 *
 * <p>Ejecuta cuatro consultas SQL independientes para obtener las series de peso,
 * NutriScore (calculado por {@link GamificacionService}), macros agrupados por semana
 * y registros de ejercicio, y las empaqueta en {@link TendenciasResponse}.</p>
 */
@Service
public class TendenciasServiceImpl implements TendenciasService {

    private final JdbcTemplate jdbcTemplate;
    private final GamificacionService gamificacionService;
    private final PerfilService perfilService;

    public TendenciasServiceImpl(JdbcTemplate jdbcTemplate, GamificacionService gamificacionService,
                               PerfilService perfilService) {
        this.jdbcTemplate = jdbcTemplate;
        this.gamificacionService = gamificacionService;
        this.perfilService = perfilService;
    }

    @Override
    @Transactional(readOnly = true)
    public TendenciasResponse getTendencias(Long usuarioId, int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate desdeHace = hoy.minusDays(Math.max(1, dias - 1));

        List<PesoTendenciaPoint> peso = obtenerPeso(usuarioId, desdeHace, hoy);
        List<NutriScoreTendenciaPoint> nutriScore = obtenerNutriScore(usuarioId, desdeHace, hoy);
        List<MacrosTendenciaPoint> macros = obtenerMacros(usuarioId, desdeHace, hoy);
        List<EjercicioTendenciaPoint> ejercicio = obtenerEjercicio(usuarioId, desdeHace, hoy);
        Double pesoObjetivo = obtenerPesoObjetivo(usuarioId);

        return new TendenciasResponse(peso, nutriScore, macros, ejercicio, pesoObjetivo);
    }

    private List<PesoTendenciaPoint> obtenerPeso(Long usuarioId, LocalDate desde, LocalDate hasta) {
        String sql = "SELECT fecha, peso_kg FROM peso_historial " +
                     "WHERE usuario_id = ? AND fecha >= ? AND fecha <= ? " +
                     "ORDER BY fecha ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new PesoTendenciaPoint(
                        rs.getDate("fecha").toLocalDate(),
                        rs.getDouble("peso_kg")
                ), usuarioId, desde, hasta);
    }

    private List<NutriScoreTendenciaPoint> obtenerNutriScore(Long usuarioId, LocalDate desde, LocalDate hasta) {
        List<NutriScoreTendenciaPoint> result = new ArrayList<>();
        String sql = "SELECT DISTINCT fecha FROM comidas " +
                     "WHERE usuario_id = ? AND fecha >= ? AND fecha <= ? " +
                     "ORDER BY fecha ASC";

        List<LocalDate> fechas = jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getDate("fecha").toLocalDate(),
                usuarioId, desde, hasta);

        // Limita a máximo 30 días para rendimiento
        int limite = Math.min(fechas.size(), 30);
        for (int i = 0; i < limite; i++) {
            LocalDate fecha = fechas.get(i);
            GamificacionResponse gam = gamificacionService.calcular(usuarioId, fecha);
            result.add(new NutriScoreTendenciaPoint(fecha, gam.getNutriScore(), gam.getNutriGrade()));
        }
        return result;
    }

    private List<MacrosTendenciaPoint> obtenerMacros(Long usuarioId, LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT
                  DATE_TRUNC('week', c.fecha) as semana_inicio,
                  to_char(DATE_TRUNC('week', c.fecha), 'IW') as numero_semana,
                  AVG(sub.kcal_dia) as kcal_promedio,
                  AVG(sub.proteinas_dia) as proteinas_promedio,
                  AVG(sub.carbos_dia) as carbos_promedio,
                  AVG(sub.grasas_dia) as grasas_promedio
                FROM (
                  SELECT c.fecha,
                    SUM(COALESCE(ca.gramos * a.kcal_por_100g / 100, 0)) as kcal_dia,
                    SUM(COALESCE(ca.gramos * a.proteinas_g / 100, 0)) as proteinas_dia,
                    SUM(COALESCE(ca.gramos * a.carbos_g / 100, 0)) as carbos_dia,
                    SUM(COALESCE(ca.gramos * a.grasas_g / 100, 0)) as grasas_dia
                  FROM comidas c
                  LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                  LEFT JOIN alimentos a ON a.id = ca.alimento_id
                  WHERE c.usuario_id = ? AND c.fecha >= ? AND c.fecha <= ?
                  GROUP BY c.fecha
                ) sub
                GROUP BY DATE_TRUNC('week', sub.fecha), numero_semana
                ORDER BY semana_inicio ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LocalDate inicioSemana = rs.getDate("semana_inicio").toLocalDate();
            String numeroSemana = rs.getString("numero_semana");
            String semana = "Sem. " + numeroSemana + " (" + inicioSemana.format(DateTimeFormatter.ofPattern("dd/MM")) + ")";

            return new MacrosTendenciaPoint(
                    semana,
                    inicioSemana,
                    rs.getDouble("kcal_promedio"),
                    rs.getDouble("proteinas_promedio"),
                    rs.getDouble("carbos_promedio"),
                    rs.getDouble("grasas_promedio")
            );
        }, usuarioId, desde, hasta);
    }

    private List<EjercicioTendenciaPoint> obtenerEjercicio(Long usuarioId, LocalDate desde, LocalDate hasta) {
        String sql = "SELECT fecha, " +
                     "COALESCE(SUM(duracion_min), 0) as duracion_min, " +
                     "COALESCE(SUM(kcal_quemadas), 0) as kcal_quemadas, " +
                     "COUNT(*) > 0 as tuvo_ejercicio " +
                     "FROM ejercicios_registro " +
                     "WHERE usuario_id = ? AND fecha >= ? AND fecha <= ? " +
                     "GROUP BY fecha " +
                     "ORDER BY fecha ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new EjercicioTendenciaPoint(
                        rs.getDate("fecha").toLocalDate(),
                        rs.getBoolean("tuvo_ejercicio"),
                        rs.getInt("duracion_min"),
                        rs.getDouble("kcal_quemadas")
                ), usuarioId, desde, hasta);
    }

    private Double obtenerPesoObjetivo(Long usuarioId) {
        try {
            return perfilService.getPerfil(usuarioId).getPesoObjetivo();
        } catch (Exception e) {
            return null;
        }
    }
}
