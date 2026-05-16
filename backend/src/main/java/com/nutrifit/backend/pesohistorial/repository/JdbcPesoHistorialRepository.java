package com.nutrifit.backend.pesohistorial.repository;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class JdbcPesoHistorialRepository implements PesoHistorialRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPesoHistorialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PesoHistorialResponse> findByUsuario(Long usuarioId, int limit) {
        String sql = """
                SELECT id, fecha, peso_kg
                FROM peso_historial
                WHERE usuario_id = ?
                ORDER BY fecha ASC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new PesoHistorialResponse(
                rs.getLong("id"),
                rs.getDate("fecha").toLocalDate().toString(),
                rs.getDouble("peso_kg")
        ), usuarioId, limit);
    }

    @Override
    public PesoHistorialResponse upsert(Long usuarioId, LocalDate fecha, double pesoKg) {
        String insertSql = """
                INSERT INTO peso_historial (usuario_id, fecha, peso_kg)
                VALUES (?, ?, ?)
                ON CONFLICT (usuario_id, fecha)
                DO UPDATE SET peso_kg = EXCLUDED.peso_kg
                """;

        jdbcTemplate.update(insertSql, usuarioId, fecha, pesoKg);

        String selectSql = """
                SELECT id, fecha, peso_kg
                FROM peso_historial
                WHERE usuario_id = ? AND fecha = ?
                """;

        List<PesoHistorialResponse> results = jdbcTemplate.query(selectSql, (rs, rowNum) -> new PesoHistorialResponse(
                rs.getLong("id"),
                rs.getDate("fecha").toLocalDate().toString(),
                rs.getDouble("peso_kg")
        ), usuarioId, fecha);

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void deleteByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        String sql = """
                DELETE FROM peso_historial
                WHERE usuario_id = ? AND fecha = ?
                """;
        jdbcTemplate.update(sql, usuarioId, fecha);
    }
}
