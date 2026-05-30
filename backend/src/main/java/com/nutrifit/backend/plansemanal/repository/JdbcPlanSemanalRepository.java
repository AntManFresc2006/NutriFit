package com.nutrifit.backend.plansemanal.repository;

import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de plan semanal.
 */
@Repository
public class JdbcPlanSemanalRepository implements PlanSemanalRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPlanSemanalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long createGenerando(Long usuarioId, LocalDate semanaInicio) {
        String sql = """
                INSERT INTO plan_semanal (usuario_id, semana_inicio, plan_json, estado, created_at)
                VALUES (?, ?, NULL, 'GENERANDO', NOW())
                ON CONFLICT (usuario_id, semana_inicio) DO UPDATE
                  SET estado = 'GENERANDO', plan_json = NULL, error_msg = NULL, created_at = NOW()
                RETURNING id
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, usuarioId, semanaInicio);
    }

    @Override
    public void updateFinalizado(Long id, String planJson, String estado, String errorMsg) {
        String sql = "UPDATE plan_semanal SET plan_json = ?, estado = ?, error_msg = ? WHERE id = ?";
        jdbcTemplate.update(sql, planJson, estado, errorMsg, id);
    }

    @Override
    public Optional<PlanSemanalResponse> findByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio) {
        String sql = """
                SELECT id, usuario_id, semana_inicio, plan_json, created_at, estado, error_msg
                FROM plan_semanal
                WHERE usuario_id = ? AND semana_inicio = ?
                """;
        try {
            PlanSemanalResponse result = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PlanSemanalResponse(
                    rs.getLong("id"),
                    rs.getObject("semana_inicio", LocalDate.class),
                    rs.getString("plan_json"),
                    rs.getObject("created_at", LocalDateTime.class),
                    rs.getString("estado"),
                    rs.getString("error_msg")
            ), usuarioId, semanaInicio);
            return Optional.of(result);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio) {
        jdbcTemplate.update(
                "DELETE FROM plan_semanal WHERE usuario_id = ? AND semana_inicio = ?",
                usuarioId, semanaInicio);
    }
}
