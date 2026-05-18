package com.nutrifit.backend.gamificacion.service;

import com.nutrifit.backend.gamificacion.dto.GamificacionResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio de gamificación. Calcula racha, NutriScore y badges basados en actividad diaria.
 */
@Service
public class GamificacionService {

    private final JdbcTemplate jdbcTemplate;
    private final PerfilService perfilService;

    public GamificacionService(JdbcTemplate jdbcTemplate, PerfilService perfilService) {
        this.jdbcTemplate = jdbcTemplate;
        this.perfilService = perfilService;
    }

    /**
     * Calcula todas las métricas de gamificación para un usuario en una fecha específica.
     * @param usuarioId ID del usuario.
     * @param fecha Fecha a evaluar.
     * @return Respuesta con racha, NutriScore, calificación y badges.
     */
    public GamificacionResponse calcular(Long usuarioId, LocalDate fecha) {
        try {
            int racha = calcularRacha(usuarioId, fecha);
            int score = calcularNutriScore(usuarioId, fecha);
            String grade = scoreToGrade(score);

            boolean cumpleProteina = verificarProteina(usuarioId, fecha);
            boolean cumpleBalance = verificarBalance(usuarioId, fecha);
            boolean cumpleEjercicio = verificarEjercicio(usuarioId, fecha);
            boolean cumpleVariedad = verificarVariedad(usuarioId, fecha);

            return new GamificacionResponse(
                    racha, score, grade,
                    cumpleProteina, cumpleBalance,
                    cumpleEjercicio, cumpleVariedad
            );
        } catch (Exception e) {
            return new GamificacionResponse(0, 0, "—", false, false, false, false);
        }
    }

    private int calcularRacha(Long usuarioId, LocalDate fecha) {
        String sql = "SELECT DISTINCT fecha FROM comidas WHERE usuario_id = ? AND fecha <= ? ORDER BY fecha DESC LIMIT 60";
        List<LocalDate> fechas = jdbcTemplate.query(
                sql,
                (rs, i) -> rs.getDate("fecha").toLocalDate(),
                usuarioId, fecha
        );

        Set<LocalDate> fechasSet = new HashSet<>(fechas);
        int racha = 0;
        LocalDate dia = fecha;
        while (fechasSet.contains(dia)) {
            racha++;
            dia = dia.minusDays(1);
        }
        return racha;
    }

    private int calcularNutriScore(Long usuarioId, LocalDate fecha) {
        int score = 0;

        if (verificarProteina(usuarioId, fecha)) {
            score += 25;
        } else if (getProteinaScore(usuarioId, fecha) >= 12) {
            score += 12;
        }

        if (verificarBalance(usuarioId, fecha)) {
            score += 25;
        } else if (getBalanceScore(usuarioId, fecha) >= 12) {
            score += 12;
        }

        if (verificarEjercicio(usuarioId, fecha)) {
            score += 25;
        }

        if (verificarVariedad(usuarioId, fecha)) {
            score += 25;
        } else if (getVariedadScore(usuarioId, fecha) >= 12) {
            score += 12;
        }

        return Math.min(score, 100);
    }

    private boolean verificarProteina(Long usuarioId, LocalDate fecha) {
        double proteinaReal = getProteinaReal(usuarioId, fecha);
        double target = getPesoActual(usuarioId) * 0.8;
        return proteinaReal >= target;
    }

    private int getProteinaScore(Long usuarioId, LocalDate fecha) {
        double proteinaReal = getProteinaReal(usuarioId, fecha);
        double target = getPesoActual(usuarioId) * 0.8;
        return proteinaReal >= target * 0.7 ? 12 : 0;
    }

    private double getProteinaReal(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT COALESCE(SUM((a.proteinas_g * ca.gramos) / 100.0), 0) AS proteinas
                FROM comidas c
                LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                LEFT JOIN alimentos a ON a.id = ca.alimento_id
                WHERE c.usuario_id = ? AND c.fecha = ?
                """;
        Double proteinas = jdbcTemplate.queryForObject(sql, Double.class, usuarioId, fecha);
        return proteinas != null ? proteinas : 0.0;
    }

    private double getPesoActual(Long usuarioId) {
        try {
            return perfilService.getPerfil(usuarioId).getPesoKgActual();
        } catch (Exception e) {
            return 70.0;
        }
    }

    private boolean verificarBalance(Long usuarioId, LocalDate fecha) {
        double balance = getBalanceReal(usuarioId, fecha);
        return Math.abs(balance) <= 300;
    }

    private int getBalanceScore(Long usuarioId, LocalDate fecha) {
        double balance = getBalanceReal(usuarioId, fecha);
        return Math.abs(balance) <= 600 ? 12 : 0;
    }

    private double getBalanceReal(Long usuarioId, LocalDate fecha) {
        try {
            double kcalConsumidas = getKcalConsumidas(usuarioId, fecha);
            double kcalQuemadas = getKcalQuemadas(usuarioId, fecha);
            double tdee = perfilService.getPerfil(usuarioId).getTdee();
            return kcalConsumidas - tdee - kcalQuemadas;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getKcalConsumidas(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT COALESCE(SUM((a.kcal_por_100g * ca.gramos) / 100.0), 0) AS kcal
                FROM comidas c
                LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                LEFT JOIN alimentos a ON a.id = ca.alimento_id
                WHERE c.usuario_id = ? AND c.fecha = ?
                """;
        Double kcal = jdbcTemplate.queryForObject(sql, Double.class, usuarioId, fecha);
        return kcal != null ? kcal : 0.0;
    }

    private double getKcalQuemadas(Long usuarioId, LocalDate fecha) {
        String sql = "SELECT COALESCE(SUM(kcal_quemadas), 0) FROM ejercicios_registro WHERE usuario_id = ? AND fecha = ?";
        Double kcal = jdbcTemplate.queryForObject(sql, Double.class, usuarioId, fecha);
        return kcal != null ? kcal : 0.0;
    }

    private boolean verificarEjercicio(Long usuarioId, LocalDate fecha) {
        String sql = "SELECT COUNT(*) FROM ejercicios_registro WHERE usuario_id = ? AND fecha = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId, fecha);
        return count != null && count > 0;
    }

    private boolean verificarVariedad(Long usuarioId, LocalDate fecha) {
        return getVariedadCount(usuarioId, fecha) >= 4;
    }

    private int getVariedadScore(Long usuarioId, LocalDate fecha) {
        int variedad = getVariedadCount(usuarioId, fecha);
        return variedad >= 2 ? 12 : 0;
    }

    private int getVariedadCount(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT COUNT(DISTINCT ca.alimento_id) AS variedad
                FROM comidas c
                INNER JOIN comida_alimentos ca ON ca.comida_id = c.id
                WHERE c.usuario_id = ? AND c.fecha = ?
                """;
        Integer variedad = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId, fecha);
        return variedad != null ? variedad : 0;
    }

    private String scoreToGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 75) return "B";
        if (score >= 60) return "C";
        if (score >= 45) return "D";
        return "F";
    }
}
