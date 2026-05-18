package com.nutrifit.backend.plansemanal.repository;

import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de plan semanal.
 * Maneja la tabla 'plan_semanal'.
 */
@Repository
public class JdbcPlanSemanalRepository implements PlanSemanalRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPlanSemanalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PlanSemanalResponse save(Long usuarioId, LocalDate semanaInicio, String planJson) {
        String sql = """
                INSERT INTO plan_semanal (usuario_id, semana_inicio, plan_json, created_at)
                VALUES (?, ?, ?, NOW())
                ON CONFLICT (usuario_id, semana_inicio) DO UPDATE SET plan_json = EXCLUDED.plan_json, created_at = NOW()
                RETURNING id, usuario_id, semana_inicio, plan_json, created_at
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PlanSemanalResponse(
                rs.getLong("id"),
                rs.getObject("semana_inicio", LocalDate.class),
                rs.getString("plan_json"),
                rs.getObject("created_at", LocalDateTime.class)
        ), usuarioId, semanaInicio, planJson);
    }

    @Override
    public Optional<PlanSemanalResponse> findByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio) {
        String sql = """
                SELECT id, usuario_id, semana_inicio, plan_json, created_at
                FROM plan_semanal
                WHERE usuario_id = ? AND semana_inicio = ?
                """;

        try {
            PlanSemanalResponse result = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PlanSemanalResponse(
                    rs.getLong("id"),
                    rs.getObject("semana_inicio", LocalDate.class),
                    rs.getString("plan_json"),
                    rs.getObject("created_at", LocalDateTime.class)
            ), usuarioId, semanaInicio);
            return Optional.of(result);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio) {
        String sql = "DELETE FROM plan_semanal WHERE usuario_id = ? AND semana_inicio = ?";
        jdbcTemplate.update(sql, usuarioId, semanaInicio);
    }
}
